package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Treasury: placeholders only, no invented numbers. Real data comes in T2 (SimpleFIN). */
@Composable
internal fun TreasuryPanel() {
    Panel(top = 140f, height = 56f, ribbon = "Treasury") {
        Box(
            Modifier
                .at(14f, 9f)
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFE0C070), Color(0xFF8A6A22)),
                        center = Offset(40f, 33f),
                        radius = 90f,
                    )
                )
                .border(2.dp, Color(0xFF5A4620), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(34.dp)) {
                val k = size.width / 34f
                val ink = Color(0xFF3A2A0A)
                drawCircle(
                    ink, radius = 10f * k, center = center,
                    style = Stroke(
                        1.6f * k, cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.5f * k, 3f * k)),
                    ),
                )
                val w = 1.6f * k
                drawLine(ink, Offset(17f * k, 10f * k), Offset(17f * k, 24f * k), w, StrokeCap.Round)
                drawLine(ink, Offset(12f * k, 13f * k), Offset(22f * k, 13f * k), w, StrokeCap.Round)
                drawLine(ink, Offset(12f * k, 20f * k), Offset(22f * k, 20f * k), w, StrokeCap.Round)
            }
        }
        MText(
            "[YOUR TOTAL]", 17f, Medieval.TextBright, Modifier.at(64f, 17f).width(160.dp),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.04f,
        )
        Box(Modifier.at(234f, 13f).width(104.dp)) {
            Cap("Quest: [GOAL]", size = 9f, spacingEm = 0.24f)
            Box(
                Modifier
                    .padding(top = 17.dp)
                    .fillMaxWidth()
                    .height(10.dp)
                    .border(1.dp, Medieval.GoldDark)
                    .background(Color(0xFF050505))
                    .padding(1.5.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0f)
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF5A2209), Color(0xFFC9772C)))
                        )
                )
            }
            MText(
                "[0%] of the way", 10f, Medieval.TextFaint,
                Modifier.padding(top = 32.dp).fillMaxWidth(), italic = true, align = TextAlign.End,
            )
        }
    }
}
