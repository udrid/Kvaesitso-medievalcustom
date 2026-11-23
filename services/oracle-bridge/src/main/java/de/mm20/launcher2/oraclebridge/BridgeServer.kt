package de.mm20.launcher2.oraclebridge

import android.util.Log
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.security.MessageDigest
import java.util.concurrent.Executors

/**
 * Tiny HTTP/1.1 server for the Oracle bridge (T24). Rules:
 * - Only Tailscale peers (100.64.0.0/10 or fd7a:115c:a1e0::/48) are answered; everything else is dropped.
 * - Every request needs "Authorization: Bearer <secret>" (secret set by the laptop over ADB).
 * - One request per connection, small JSON bodies (max 64 KB).
 */
class BridgeServer(
    private val agent: OracleAgentService,
    val secret: String,
    private val port: Int,
) {
    private var socket: ServerSocket? = null
    private val pool = Executors.newFixedThreadPool(3)
    private val actions = AgentActions(agent)

    fun isRunning(): Boolean = socket?.isClosed == false

    fun start() {
        Thread({
            try {
                val ss = ServerSocket(port)
                socket = ss
                Log.i(Tag, "oracle bridge listening on $port")
                while (!ss.isClosed) {
                    val client = ss.accept()
                    if (!isTailscale(client.inetAddress)) {
                        client.close()
                        continue
                    }
                    pool.execute { handle(client) }
                }
            } catch (e: SocketException) {
                Log.i(Tag, "oracle bridge stopped: ${e.message}")
            } catch (e: Exception) {
                Log.e(Tag, "oracle bridge server failed", e)
            }
        }, "oracle-bridge").start()
    }

    fun stop() {
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.e(Tag, "oracle bridge close", e)
        }
        socket = null
        pool.shutdownNow()
    }

    private fun handle(client: Socket) {
        client.use { s ->
            s.soTimeout = 15000
            try {
                val input = BufferedInputStream(s.getInputStream())
                val out = s.getOutputStream()
                val requestLine = readLine(input) ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return respond(out, 400, json("error" to "bad request"))
                val method = parts[0]
                val target = parts[1]
                val headers = mutableMapOf<String, String>()
                while (true) {
                    val line = readLine(input) ?: break
                    if (line.isEmpty()) break
                    val i = line.indexOf(':')
                    if (i > 0) headers[line.substring(0, i).trim().lowercase()] = line.substring(i + 1).trim()
                }
                val auth = headers["authorization"]?.removePrefix("Bearer ")?.trim() ?: ""
                if (!constantTimeEquals(auth, secret)) {
                    Log.e(Tag, "oracle bridge: bad secret from ${s.inetAddress.hostAddress}")
                    return respond(out, 401, json("error" to "unauthorized"))
                }
                val length = headers["content-length"]?.toIntOrNull() ?: 0
                if (length > 65536) return respond(out, 413, json("error" to "body too large"))
                val body = if (length > 0) String(readBytes(input, length)) else ""
                val path = target.substringBefore('?')
                val query = parseQuery(target.substringAfter('?', ""))
                val params = if (body.isNotBlank()) JSONObject(body) else JSONObject()
                for ((k, v) in query) if (!params.has(k)) params.put(k, v)
                val result = actions.run(method, path, params)
                when (result) {
                    is AgentResult.Json -> respond(out, result.status, result.body.toString().toByteArray(), "application/json")
                    is AgentResult.Image -> respond(out, 200, result.bytes, result.mime)
                }
            } catch (e: Exception) {
                Log.e(Tag, "oracle bridge request failed", e)
                try {
                    respond(s.getOutputStream(), 500, json("error" to (e.message ?: e.javaClass.simpleName)))
                } catch (e2: Exception) {
                    Log.e(Tag, "oracle bridge could not send error", e2)
                }
            }
        }
    }

    private fun respond(out: OutputStream, status: Int, body: JSONObject) =
        respond(out, status, body.toString().toByteArray(), "application/json")

    private fun respond(out: OutputStream, status: Int, bytes: ByteArray, mime: String) {
        val head = "HTTP/1.1 $status ${if (status == 200) "OK" else "Error"}\r\n" +
            "Content-Type: $mime\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
        out.write(head.toByteArray())
        out.write(bytes)
        out.flush()
    }

    private fun readLine(input: InputStream): String? {
        val buf = ByteArrayOutputStream()
        while (true) {
            val b = input.read()
            if (b == -1) return if (buf.size() == 0) null else buf.toString()
            if (b == '\n'.code) return buf.toString().trimEnd('\r')
            buf.write(b)
            if (buf.size() > 8192) return null
        }
    }

    private fun readBytes(input: InputStream, n: Int): ByteArray {
        val data = ByteArray(n)
        var read = 0
        while (read < n) {
            val r = input.read(data, read, n - read)
            if (r == -1) break
            read += r
        }
        return data.copyOf(read)
    }

    private fun parseQuery(q: String): Map<String, String> =
        q.split('&').filter { it.contains('=') }.associate {
            java.net.URLDecoder.decode(it.substringBefore('='), "UTF-8") to
                java.net.URLDecoder.decode(it.substringAfter('='), "UTF-8")
        }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

    companion object {
        fun isTailscale(addr: InetAddress?): Boolean {
            val b = addr?.address ?: return false
            return when (addr) {
                is Inet4Address -> (b[0].toInt() and 0xFF) == 100 && (b[1].toInt() and 0xC0) == 64
                is Inet6Address -> b.size == 16 && (b[0].toInt() and 0xFF) == 0xfd && (b[1].toInt() and 0xFF) == 0x7a &&
                    (b[2].toInt() and 0xFF) == 0x11 && (b[3].toInt() and 0xFF) == 0x5c &&
                    (b[4].toInt() and 0xFF) == 0xa1 && (b[5].toInt() and 0xFF) == 0xe0
                else -> false
            }
        }

        fun json(vararg pairs: Pair<String, Any?>): JSONObject = JSONObject().apply { pairs.forEach { put(it.first, it.second) } }
    }
}
