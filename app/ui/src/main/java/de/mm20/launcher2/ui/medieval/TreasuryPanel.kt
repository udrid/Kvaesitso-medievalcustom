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
    val land = LocalLandscape.current
    Panel(top = 0f, height = if (land) 66f else 56f, ribbon = "Treasury") {
        Box(
            Modifier
                .at(if (land) 12f else 14f, if (land) 14f else 9f)
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Medieval.palette.coinA, Medieval.palette.coinB),
                        center = Offset(40f, 33f),
                        radius = 90f,
                    )
                )
                .border(2.dp, Medieval.palette.coinRim, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(34.dp)) {
                val k = size.width / 34f
                val ink = Medieval.palette.coinInk
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
        if (land) {
            // landscape column (222 wide): total on top, quest line, bar and percentage under it
            MText(
                "[YOUR TOTAL]", 15f, Medieval.TextBright, Modifier.at(54f, 13f).width((LocalColumnWidth.current - 66f).dp),
                font = Medieval.Cinzel, bold = true, spacingEm = 0.04f,
            )
            Box(Modifier.at(54f, 36f).width((LocalColumnWidth.current - 66f).dp)) {
                Cap("Quest: [GOAL]", size = 8.5f, spacingEm = 0.12f)
                MText(
                    "[0%] of the way", 9.5f, Medieval.TextFaint,
                    Modifier.fillMaxWidth(), italic = true, align = TextAlign.End,
                )
                Box(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .height(9.dp)
                        .border(1.dp, Medieval.GoldDark)
                        .background(Color(0xFF050505))
                        .padding(1.5.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0f)
                            .background(Brush.horizontalGradient(listOf(Medieval.palette.barA, Medieval.palette.barB)))
                    )
                }
            }
            return@Panel
        }
        MText(
            "[YOUR TOTAL]", 16f, Medieval.TextBright, Modifier.at(62f, 18f).width(130.dp),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.04f,
        )
        Box(Modifier.at(197f, 9f).width(118.dp)) {
            Cap("Quest: [GOAL]", size = 11f, spacingEm = 0.14f)
            Box(
                Modifier
                    .padding(top = 19.dp)
                    .fillMaxWidth()
                    .height(12.dp)
                    .border(1.dp, Medieval.GoldDark)
                    .background(Color(0xFF050505))
                    .padding(1.5.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0f)
                        .background(
                            Brush.horizontalGradient(listOf(Medieval.palette.barA, Medieval.palette.barB))
                        )
                )
            }
            MText(
                "[0%] of the way", 10f, Medieval.TextFaint,
                Modifier.padding(top = 31.dp).fillMaxWidth(), italic = true, align = TextAlign.End,
            )
        }
    }
}
