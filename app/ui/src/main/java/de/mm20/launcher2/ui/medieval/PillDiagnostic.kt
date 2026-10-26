package de.mm20.launcher2.ui.medieval

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * TEMPORARY (T5c-pill-position): two lines of numbers under the camera pill so the owner can
 * read how the pill was placed without a computer. Remove by reverting the commit that added it.
 */

/** The phone maker's cutout outline string (display pixels), shortened; "-" if not readable. */
@SuppressLint("DiscouragedApi")
internal fun systemCutoutSpec(): String {
    val res = Resources.getSystem()
    val id = res.getIdentifier("config_mainBuiltInDisplayCutout", "string", "android")
    if (id == 0) return "-"
    return try {
        res.getString(id).trim().replace(Regex("\\s+"), " ").take(60).ifEmpty { "-" }
    } catch (e: Resources.NotFoundException) {
        Log.e("medieval", "Pill diag: cutout spec not readable", e)
        "-"
    }
}

/** Small text block whose top edge is at [topPx] window pixels inside the pill's box. */
@Composable
internal fun PillDiagnosticText(text: String, topPx: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, topPx) },
        contentAlignment = Alignment.TopCenter,
    ) {
        BasicText(
            text,
            Modifier
                .background(Color(0xCC000000))
                .padding(horizontal = 4.dp, vertical = 1.dp),
            style = TextStyle(
                color = Color(0xFFE0B25C),
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
