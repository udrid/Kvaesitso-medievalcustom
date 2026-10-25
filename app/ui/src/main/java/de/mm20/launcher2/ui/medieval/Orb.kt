package de.mm20.launcher2.ui.medieval

import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import de.mm20.launcher2.ui.R

/** Package name of the Claude Android app. UNVERIFIED until the owner tests the orb on the phone. */
internal const val ClaudePackage = "com.anthropic.claude"

private const val RingText = "✠ VERITAS ET CONSILIUM ✠ ANNO DOMINI ✠ ORACULUM ✠"

/** The Oracle: ring with inscription, glowing orb, label. Tap opens the Claude app. */
@Composable
internal fun OracleOrb() {
    val context = LocalContext.current
    val ringFace = androidx.compose.runtime.remember {
        try {
            ResourcesCompat.getFont(context, R.font.cinzel_variable)
        } catch (e: Exception) {
            Log.e("medieval", "Could not load Cinzel for the orb ring", e)
            null
        }
    }

    // ring with inscription (176 x 176 at 107, 258)
    Canvas(Modifier.at(107f, 258f).size(176.dp)) {
        val k = size.width / 176f
        drawCircle(Medieval.GoldDark, radius = 86f * k, center = center, style = Stroke(1.5f * k))
        drawCircle(Color(0xFF3A2E18), radius = 57f * k, center = center, style = Stroke(1f * k))
        drawIntoCanvas { canvas ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Medieval.GoldDeep.toArgb()
                textSize = 8.5f * k
                letterSpacing = 0.4f
                isFakeBoldText = true
                if (ringFace != null) this.typeface = ringFace
            }
            val path = android.graphics.Path().apply {
                moveTo(17f * k, 88f * k)
                arcTo(RectF(17f * k, 17f * k, 159f * k, 159f * k), 180f, 359.9f)
            }
            canvas.nativeCanvas.drawTextOnPath(RingText, path, 0f, 0f, paint)
        }
    }

    // orb (124 at 133, 284)
    Box(
        Modifier
            .at(133f, 284f)
            .size(124.dp)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(
                        0.6f to Color(0x42E58A3C),
                        1f to Color.Transparent,
                        center = center,
                        radius = size.width * 0.75f,
                    ),
                    radius = size.width * 0.75f,
                )
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    0f to Color(0xFF4A2D14),
                    0.72f to Color(0xFF0F0904),
                    center = Offset(62f * 3f, 47f * 3f),
                    radius = 124f * 3f,
                )
            )
            .border(2.dp, Medieval.GoldDeep, CircleShape)
            .clickable { openClaude(context) },
        contentAlignment = Alignment.Center,
    ) {
        SvgIcon(MedievalIcons.Flame, 54f, Medieval.Ember, strokeWidth = 1.4f)
    }

    MText(
        "The Oracle", 19f, Medieval.Text,
        Modifier.at(95f, 436f).width(200.dp),
        font = Medieval.Blackletter, bold = true, align = TextAlign.Center,
    )
}

internal fun openClaude(context: android.content.Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(ClaudePackage)
    if (intent == null) {
        Log.e("medieval", "Claude app ($ClaudePackage) has no launch intent")
        Toast.makeText(context, "The Claude app is not installed", Toast.LENGTH_SHORT).show()
        return
    }
    launchSafely(context, intent, "the Claude app")
}
