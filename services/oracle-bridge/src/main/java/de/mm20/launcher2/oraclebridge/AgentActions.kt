package de.mm20.launcher2.oraclebridge

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityNodeInfo
import de.mm20.launcher2.notifications.NotificationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

sealed class AgentResult {
    data class Json(val body: JSONObject, val status: Int = 200) : AgentResult()
    class Image(val bytes: ByteArray, val mime: String) : AgentResult()
}

/**
 * What the laptop can ask for (T24). All endpoints take JSON bodies or query parameters.
 *  GET  /ping                         -> {ok, package, screenOn}
 *  GET  /screenshot?scale=0.5&q=70    -> JPEG (q) or PNG (q=0)
 *  GET  /ui?all=0                     -> visible nodes: text, desc, id, bounds, flags
 *  POST /tap {x,y} | {text}           -> taps a point, or clicks the first node whose text/desc contains `text`
 *  POST /longpress {x,y}
 *  POST /swipe {x1,y1,x2,y2,ms}
 *  POST /type {text, append:false}    -> sets text in the focused field
 *  POST /key {key: home|back|recents|notifications|quicksettings|lock|screenshot}
 *  POST /launch {package}             -> opens an app
 *  POST /open {url}                   -> opens a link or intent URI
 *  GET  /notifs                       -> active notifications (title, text, package, time)
 *  POST /adb                          -> turn ADB wireless debugging on again (needs WRITE_SECURE_SETTINGS)
 */
class AgentActions(private val agent: OracleAgentService) : KoinComponent {

    private val notifications: NotificationRepository by inject()
    private val main = Handler(Looper.getMainLooper())

    fun run(method: String, path: String, p: JSONObject): AgentResult = when (path) {
        "/ping" -> ok("package" to agent.lastPackage, "screenOn" to screenOn())
        "/screenshot" -> screenshot(p.optDouble("scale", 0.5), p.optInt("q", 70))
        "/ui" -> ok("package" to agent.lastPackage, "nodes" to uiTree(p.optBoolean("all", false)))
        "/tap" -> if (p.has("text")) tapText(p.getString("text")) else gesture(tapPath(p), 60)
        "/longpress" -> gesture(tapPath(p), 700)
        "/swipe" -> {
            val path2 = Path().apply {
                moveTo(p.getDouble("x1").toFloat(), p.getDouble("y1").toFloat())
                lineTo(p.getDouble("x2").toFloat(), p.getDouble("y2").toFloat())
            }
            gesture(path2, p.optLong("ms", 300))
        }
        "/type" -> type(p.getString("text"), p.optBoolean("append", false))
        "/key" -> key(p.getString("key"))
        "/launch" -> launch(p.getString("package"))
        "/open" -> open(p.getString("url"))
        "/notifs" -> ok("notifications" to notifs())
        "/adb" -> ok("adb" to OracleAgentService.ensureAdbWifi(agent))
        else -> AgentResult.Json(BridgeServer.json("error" to "unknown path $path"), 404)
    }

    private fun ok(vararg pairs: Pair<String, Any?>) = AgentResult.Json(BridgeServer.json("ok" to true, *pairs))
    private fun fail(msg: String) = AgentResult.Json(BridgeServer.json("ok" to false, "error" to msg), 400)

    private fun screenOn(): Boolean =
        (agent.getSystemService(android.os.PowerManager::class.java)).isInteractive

    private fun screenshot(scale: Double, quality: Int): AgentResult {
        val latch = CountDownLatch(1)
        var bytes: ByteArray? = null
        var error: String? = null
        agent.takeScreenshot(Display.DEFAULT_DISPLAY, agent.mainExecutor, object : AccessibilityService.TakeScreenshotCallback {
            override fun onSuccess(result: AccessibilityService.ScreenshotResult) {
                try {
                    val hw = Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                    result.hardwareBuffer.close()
                    if (hw == null) {
                        error = "no bitmap"
                    } else {
                        val soft = hw.copy(Bitmap.Config.ARGB_8888, false)
                        val s = scale.coerceIn(0.1, 1.0)
                        val scaled = if (s < 1.0) Bitmap.createScaledBitmap(soft, (soft.width * s).toInt(), (soft.height * s).toInt(), true) else soft
                        val out = ByteArrayOutputStream()
                        if (quality <= 0) scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
                        else scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(10, 100), out)
                        bytes = out.toByteArray()
                    }
                } catch (e: Exception) {
                    Log.e(Tag, "screenshot encode", e)
                    error = e.message
                } finally {
                    latch.countDown()
                }
            }

            override fun onFailure(errorCode: Int) {
                Log.e(Tag, "screenshot failed code $errorCode")
                error = "screenshot failed (code $errorCode; 3 = too soon, wait 1 s)"
                latch.countDown()
            }
        })
        latch.await(5, TimeUnit.SECONDS)
        val b = bytes ?: return fail(error ?: "screenshot timed out")
        return AgentResult.Image(b, if (quality <= 0) "image/png" else "image/jpeg")
    }

    private fun roots(): List<AccessibilityNodeInfo> {
        val list = agent.windows.mapNotNull { it.root }
        return list.ifEmpty { listOfNotNull(agent.rootInActiveWindow) }
    }

    private fun uiTree(all: Boolean): JSONArray {
        val arr = JSONArray()
        val r = Rect()
        fun walk(n: AccessibilityNodeInfo?, depth: Int) {
            if (n == null || depth > 60 || arr.length() > 600) return
            if (!all && !n.isVisibleToUser) return
            val text = n.text?.toString()
            val desc = n.contentDescription?.toString()
            if (all || !text.isNullOrBlank() || !desc.isNullOrBlank() || n.isClickable || n.isEditable || n.isScrollable) {
                n.getBoundsInScreen(r)
                arr.put(JSONObject().apply {
                    text?.takeIf { it.isNotBlank() }?.let { put("t", it.take(200)) }
                    desc?.takeIf { it.isNotBlank() }?.let { put("d", it.take(200)) }
                    n.viewIdResourceName?.let { put("id", it.substringAfter(":id/")) }
                    put("b", JSONArray(listOf(r.left, r.top, r.right, r.bottom)))
                    val flags = buildString {
                        if (n.isClickable) append('c')
                        if (n.isEditable) append('e')
                        if (n.isScrollable) append('s')
                        if (n.isFocused) append('f')
                        if (n.isCheckable) append(if (n.isChecked) 'X' else 'x')
                    }
                    if (flags.isNotEmpty()) put("f", flags)
                })
            }
            for (i in 0 until n.childCount) walk(n.getChild(i), depth + 1)
        }
        roots().forEach { walk(it, 0) }
        return arr
    }

    private fun findByText(query: String): AccessibilityNodeInfo? {
        val q = query.lowercase()
        var best: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo?) {
            if (n == null || best != null) return
            val t = n.text?.toString()?.lowercase()
            val d = n.contentDescription?.toString()?.lowercase()
            if (n.isVisibleToUser && ((t != null && t.contains(q)) || (d != null && d.contains(q)))) {
                best = n
                return
            }
            for (i in 0 until n.childCount) walk(n.getChild(i))
        }
        roots().forEach { walk(it) }
        return best
    }

    private fun tapText(text: String): AgentResult {
        val node = findByText(text) ?: return fail("nothing on screen contains '$text'")
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) target = target.parent
        if (target != null && target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return ok("clicked" to (node.text ?: node.contentDescription)?.toString())
        }
        val r = Rect().also { node.getBoundsInScreen(it) }
        return gesture(Path().apply { moveTo(r.exactCenterX(), r.exactCenterY()) }, 60)
    }

    private fun tapPath(p: JSONObject) = Path().apply { moveTo(p.getDouble("x").toFloat(), p.getDouble("y").toFloat()) }

    private fun gesture(path: Path, ms: Long): AgentResult {
        val latch = CountDownLatch(1)
        var done = false
        val g = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, ms.coerceIn(1, 10000))).build()
        main.post {
            val sent = agent.dispatchGesture(g, object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) { done = true; latch.countDown() }
                override fun onCancelled(gestureDescription: GestureDescription?) { latch.countDown() }
            }, null)
            if (!sent) latch.countDown()
        }
        latch.await(ms + 3000, TimeUnit.MILLISECONDS)
        return if (done) ok() else fail("gesture was cancelled")
    }

    /** The field to type into: the focused editable node, else the first visible editable node on screen. */
    private fun editableField(): AccessibilityNodeInfo? {
        roots().firstNotNullOfOrNull { it.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) }?.takeIf { it.isEditable }?.let { return it }
        var found: AccessibilityNodeInfo? = null
        fun walk(n: AccessibilityNodeInfo?) {
            if (n == null || found != null) return
            if (n.isEditable && n.isVisibleToUser) { found = n; return }
            for (i in 0 until n.childCount) walk(n.getChild(i))
        }
        roots().forEach { walk(it) }
        return found
    }

    private fun type(text: String, append: Boolean): AgentResult {
        val node = editableField() ?: return fail("no text field on screen (tap one first)")
        val current = if (append && !node.isShowingHintText) node.text?.toString() ?: "" else ""
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, current + text) }
        return if (node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) ok() else fail("field refused the text")
    }

    private fun key(name: String): AgentResult {
        val action = when (name.lowercase()) {
            "home" -> AccessibilityService.GLOBAL_ACTION_HOME
            "back" -> AccessibilityService.GLOBAL_ACTION_BACK
            "recents" -> AccessibilityService.GLOBAL_ACTION_RECENTS
            "notifications" -> AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
            "quicksettings" -> AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
            "lock" -> AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
            "screenshot" -> AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT
            "enter" -> return pressEnter()
            else -> return fail("unknown key $name")
        }
        return if (agent.performGlobalAction(action)) ok() else fail("key $name refused")
    }

    private fun pressEnter(): AgentResult {
        val node = editableField() ?: return fail("no text field on screen")
        val ime = AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id
        return if (node.performAction(ime)) ok() else fail("enter refused")
    }

    private fun launch(pkg: String): AgentResult {
        val intent = agent.packageManager.getLaunchIntentForPackage(pkg) ?: return fail("no app $pkg")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        agent.startActivity(intent)
        return ok("launched" to pkg)
    }

    private fun open(url: String): AgentResult {
        val intent = if (url.startsWith("intent:")) Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        else Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        agent.startActivity(intent)
        return ok("opened" to url)
    }

    private fun notifs(): JSONArray {
        val list = runBlocking { notifications.notifications.first() }
        val arr = JSONArray()
        list.sortedByDescending { it.postTime }.forEach { n ->
            arr.put(JSONObject().apply {
                put("package", n.packageName)
                put("time", n.postTime)
                put("title", n.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString())
                put("text", n.extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()?.take(500))
                put("key", n.key)
            })
        }
        return arr
    }
}
