package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** What the Treasury panel shows (T2, owner 2026-11-23: total = net worth, quest = pay off debt). */
data class TreasuryState(
    val connected: Boolean = false,
    val netWorth: Double? = null,
    val debt: Double? = null,
    /** Debt when the quest started; progress = (start - debt) / start. */
    val debtStart: Double? = null,
    val updated: Long = 0L,
    val error: String? = null,
) {
    val progress: Float?
        get() {
            val s = debtStart ?: return null
            val d = debt ?: return null
            if (s <= 0.0) return if (d <= 0.0) 1f else 0f
            return ((s - d) / s).toFloat().coerceIn(0f, 1f)
        }
}

/**
 * SimpleFIN Bridge (read-only bank data). The owner signs up and links banks on the SimpleFIN site and
 * pastes the one-time Setup Token here; Claude never sees bank logins. The Access URL (it holds the
 * credentials) is stored encrypted with an Android Keystore key. At most one refresh per [StaleMs]
 * (SimpleFIN allows about 24 calls a day).
 *
 * UNVERIFIED until the owner connects: whether credit card and loan balances come back negative.
 * Net worth = sum of balances; debt = sum of the negative balances.
 */
object TreasuryData {
    private const val Prefs = "medieval_treasury"
    private const val KeyAccess = "access_enc"
    private const val KeyDebtStart = "debt_start"
    private const val KeyNet = "net"
    private const val KeyDebt = "debt"
    private const val KeyUpdated = "updated"
    private const val KeyAlias = "deliverance_treasury"
    private const val StaleMs = 6 * 60 * 60 * 1000L

    private val _state = MutableStateFlow(TreasuryState())
    val state: StateFlow<TreasuryState> = _state
    private var loaded = false

    private fun prefs(c: Context) = c.getSharedPreferences(Prefs, Context.MODE_PRIVATE)

    fun load(c: Context) {
        if (loaded) return
        loaded = true
        val p = prefs(c)
        _state.value = TreasuryState(
            connected = p.contains(KeyAccess),
            netWorth = p.getString(KeyNet, null)?.toDoubleOrNull(),
            debt = p.getString(KeyDebt, null)?.toDoubleOrNull(),
            debtStart = p.getString(KeyDebtStart, null)?.toDoubleOrNull(),
            updated = p.getLong(KeyUpdated, 0L),
        )
    }

    /** Claims the Setup Token (POST to the decoded claim URL) and saves the Access URL. Returns an error text or null. */
    suspend fun connect(c: Context, setupToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val claimUrl = String(Base64.decode(setupToken.trim(), Base64.DEFAULT)).trim()
            if (!claimUrl.startsWith("https://")) return@withContext "That doesn't look like a SimpleFIN setup token."
            val conn = (URL(claimUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setFixedLengthStreamingMode(0)
                connectTimeout = 15000; readTimeout = 20000
            }
            val code = conn.responseCode
            if (code == 403) return@withContext "This token was already used or doesn't exist. Make a new one on the SimpleFIN site."
            if (code != 200) return@withContext "SimpleFIN answered $code."
            val access = conn.inputStream.bufferedReader().readText().trim()
            prefs(c).edit().putString(KeyAccess, encrypt(access)).remove(KeyDebtStart).apply()
            _state.value = _state.value.copy(connected = true, error = null)
            refresh(c, force = true)
            null
        } catch (e: Exception) {
            Log.e("medieval", "treasury connect failed", e)
            "Could not connect: ${e.javaClass.simpleName}"
        }
    }

    fun disconnect(c: Context) {
        prefs(c).edit().clear().apply()
        _state.value = TreasuryState()
    }

    /** Fetches balances if the last fetch is older than 6 hours (or [force]). */
    suspend fun refresh(c: Context, force: Boolean = false) = withContext(Dispatchers.IO) {
        load(c)
        val p = prefs(c)
        val enc = p.getString(KeyAccess, null) ?: return@withContext
        if (!force && System.currentTimeMillis() - p.getLong(KeyUpdated, 0L) < StaleMs) return@withContext
        try {
            val access = URL(decrypt(enc))
            val userInfo = access.userInfo ?: throw IllegalStateException("access URL has no credentials")
            val base = URL(access.protocol, access.host, access.port, access.path.trimEnd('/') + "/accounts?balances-only=1&version=2")
            val conn = (base.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Authorization", "Basic " + Base64.encodeToString(userInfo.toByteArray(), Base64.NO_WRAP))
                connectTimeout = 15000; readTimeout = 30000
            }
            val code = conn.responseCode
            if (code != 200) {
                val msg = when (code) { 402 -> "SimpleFIN subscription needs payment"; 403 -> "SimpleFIN access was revoked"; else -> "SimpleFIN answered $code" }
                _state.value = _state.value.copy(error = msg)
                return@withContext
            }
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val accounts = json.optJSONArray("accounts")
            var net = 0.0
            var debt = 0.0
            for (i in 0 until (accounts?.length() ?: 0)) {
                val bal = accounts!!.getJSONObject(i).optString("balance").toDoubleOrNull() ?: continue
                net += bal
                if (bal < 0) debt += -bal
            }
            val errList = json.optJSONArray("errlist")
            val err = if (errList != null && errList.length() > 0) errList.getJSONObject(0).optString("msg").take(120) else null
            val start = p.getString(KeyDebtStart, null)?.toDoubleOrNull() ?: debt
            val now = System.currentTimeMillis()
            p.edit().putString(KeyNet, net.toString()).putString(KeyDebt, debt.toString())
                .putString(KeyDebtStart, maxOf(start, debt).toString()).putLong(KeyUpdated, now).apply()
            _state.value = TreasuryState(true, net, debt, maxOf(start, debt), now, err)
        } catch (e: Exception) {
            Log.e("medieval", "treasury refresh failed", e)
            _state.value = _state.value.copy(error = "Could not refresh")
        }
    }

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getEntry(KeyAlias, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(KeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return gen.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val out = cipher.iv + cipher.doFinal(plain.toByteArray())
        return Base64.encodeToString(out, Base64.NO_WRAP)
    }

    private fun decrypt(enc: String): String {
        val all = Base64.decode(enc, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, all, 0, 12))
        }
        return String(cipher.doFinal(all, 12, all.size - 12))
    }
}
