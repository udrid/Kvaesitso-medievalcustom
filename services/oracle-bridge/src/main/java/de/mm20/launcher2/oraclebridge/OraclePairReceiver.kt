package de.mm20.launcher2.oraclebridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Pairing from the laptop (T24). The manifest guards this receiver with WRITE_SECURE_SETTINGS, so only
 * the ADB shell (or a system app) can send to it; ordinary apps cannot set or read the secret.
 *
 *   adb shell am broadcast -a de.mm20.launcher2.oraclebridge.PAIR --es secret <64 hex chars> -n <pkg>/de.mm20.launcher2.oraclebridge.OraclePairReceiver
 *   adb shell am broadcast -a de.mm20.launcher2.oraclebridge.UNPAIR -n <pkg>/de.mm20.launcher2.oraclebridge.OraclePairReceiver
 *   adb shell am broadcast -a de.mm20.launcher2.oraclebridge.STATUS -n <pkg>/de.mm20.launcher2.oraclebridge.OraclePairReceiver
 * The result line printed by `am broadcast` carries the answer.
 */
class OraclePairReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ActionPair -> {
                val secret = intent.getStringExtra("secret")
                if (secret == null || secret.length < 32 || !secret.all { it.isLetterOrDigit() }) {
                    Log.e(Tag, "pair: rejected secret (needs 32+ letters or digits)")
                    setResultData("error: secret must be 32+ letters or digits")
                    return
                }
                BridgePrefs.setSecret(context, secret)
                BridgePrefs.setEnabled(context, true)
                setResultData("paired; agent=${OracleAgentService.getInstance() != null}")
            }
            ActionUnpair -> {
                BridgePrefs.setSecret(context, null)
                OracleAgentService.getInstance()?.refreshServer()
                setResultData("unpaired")
            }
            ActionStatus -> {
                val agent = OracleAgentService.getInstance()
                setResultData(
                    "paired=${BridgePrefs.isPaired(context)} enabled=${BridgePrefs.isEnabled(context)} " +
                        "agent=${agent != null} listening=${agent?.isListening() == true} port=${BridgePrefs.Port}"
                )
            }
        }
    }

    companion object {
        const val ActionPair = "de.mm20.launcher2.oraclebridge.PAIR"
        const val ActionUnpair = "de.mm20.launcher2.oraclebridge.UNPAIR"
        const val ActionStatus = "de.mm20.launcher2.oraclebridge.STATUS"
    }
}

internal const val Tag = "medieval"
