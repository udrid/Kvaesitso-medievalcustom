package de.mm20.launcher2.ui.medieval

import android.content.Intent
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val WaxPath =
    "M27 2c5 0 7 3 11 4s8 4 9 9 4 6 4 12-3 8-4 12-5 8-10 9-8 3-14 3-9-3-12-8-5-8-5-13 2-8 4-12 5-9 10-10 6-6 10-6z"

@Composable
private fun RowScope.WaxSeal(icon: List<String>, name: String, label: String, intent: () -> Intent) {
    val context = LocalContext.current
    val tap = rememberTapReaction()
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            Modifier
                .size(54.dp)
                // T9: the seal presses in like a stamp
                .tapReaction(tap, press = true)
                .clickable(interactionSource = tap.source, indication = null) { tap.open { launchSafely(context, intent(), name) } },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(54.dp)) {
                val k = size.width / 54f
                val wax = androidx.compose.ui.graphics.vector.PathParser().parsePathString(WaxPath).toPath()
                val m = androidx.compose.ui.graphics.Matrix()
                m.scale(k, k)
                wax.transform(m)
                drawPath(wax, Medieval.Wax)
                drawCircle(Color(0xFF2A0A08), radius = 19f * k, center = center, style = Stroke(2f * k))
                drawCircle(
                    Color(0x2EFFBE96), radius = 19f * k, center = Offset(center.x - 0.6f * k, center.y - 0.6f * k),
                    style = Stroke(1f * k),
                )
            }
            SvgIcon(icon, 24f, Color(0xFFE3CF9C))
        }
        MText(label, 8.5f, Medieval.Cap, font = Medieval.Cinzel, bold = true, spacingEm = 0.2f)
    }
}

/** Divider ornament and four oxblood wax seals: Herald, Portrait, Road, Smithy. */
@Composable
internal fun Dock() {
    Canvas(Modifier.at(0f, 551f).size(361.dp, 10.dp)) {
        val line = Medieval.GoldLine
        drawLine(line, Offset(0f, 5f * density), Offset(161f * density, 5f * density), density)
        drawLine(line, Offset(199f * density, 5f * density), Offset(361f * density, 5f * density), density)
        val gold = Medieval.GoldDeep
        drawLine(gold, Offset(180f * density, 0f), Offset(180f * density, 10f * density), 1.6f * density)
        drawLine(gold, Offset(175f * density, 4f * density), Offset(185f * density, 4f * density), 1.6f * density)
    }
    Row(
        Modifier.at(0f, 566f).width(361.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        WaxSeal(MedievalIcons.Phone, "Phone", "HERALD") { Intent(Intent.ACTION_DIAL) }
        WaxSeal(MedievalIcons.Camera, "Camera", "PORTRAIT") { Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA) }
        WaxSeal(MedievalIcons.Compass, "Browser", "ROAD") {
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
        }
        WaxSeal(MedievalIcons.Anvil, "Settings", "SMITHY") { Intent(Settings.ACTION_SETTINGS) }
    }
}
