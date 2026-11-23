package de.mm20.launcher2.oraclebridge

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

/** One message from Claude for the owner (T25 "Claude's Scroll"). */
data class ScrollEntry(val text: String, val time: Long)

/**
 * Claude's Scroll (T25): messages the laptop writes through the Oracle bridge (POST /scroll).
 * The newest one is shown as the first line of Dispatches (owner pick, 2026-11-23). Kept in
 * app-private preferences, newest last, at most [Max] entries.
 */
object ScrollStore {
    private const val Name = "oracle_scroll"
    private const val Key = "entries"
    private const val Max = 20

    private val _entries = MutableStateFlow<List<ScrollEntry>>(emptyList())
    val entries: StateFlow<List<ScrollEntry>> = _entries
    private var loaded = false

    fun load(context: Context) {
        if (loaded) return
        loaded = true
        val raw = context.getSharedPreferences(Name, Context.MODE_PRIVATE).getString(Key, null) ?: return
        _entries.value = try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getJSONObject(it).let { o -> ScrollEntry(o.getString("text"), o.getLong("time")) } }
        } catch (e: Exception) {
            Log.e(Tag, "scroll: could not read saved entries", e)
            emptyList()
        }
    }

    fun add(context: Context, text: String) {
        load(context)
        save(context, (_entries.value + ScrollEntry(text.take(2000), System.currentTimeMillis())).takeLast(Max))
    }

    /** Removes the newest entry (swipe on the Dispatches line). */
    fun dismissLatest(context: Context) {
        load(context)
        save(context, _entries.value.dropLast(1))
    }

    fun clear(context: Context) = save(context, emptyList())

    private fun save(context: Context, list: List<ScrollEntry>) {
        _entries.value = list
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("text", it.text).put("time", it.time)) }
        context.getSharedPreferences(Name, Context.MODE_PRIVATE).edit().putString(Key, arr.toString()).apply()
    }
}
