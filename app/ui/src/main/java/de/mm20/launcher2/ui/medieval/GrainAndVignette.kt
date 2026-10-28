package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.random.Random

private fun noiseBitmap(): Bitmap {
    val size = 160
    val rnd = Random(11)
    val pixels = IntArray(size * size) {
        // warm tan speckle, random strength (matches the C5 grain tint 0.6/0.5/0.35)
        val a = rnd.nextInt(0, 60)
        (a shl 24) or (0xC8 shl 16) or (0xA0 shl 8) or 0x70
    }
    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
}

/** Film grain and dark vignette over the whole screen. Does not take touches. */
@Composable
internal fun GrainAndVignette(modifier: Modifier = Modifier, rim: Float = 0.72f) {
    val grain = remember { ShaderBrush(ImageShader(noiseBitmap().asImageBitmap(), TileMode.Repeated, TileMode.Repeated)) }
    Canvas(modifier.fillMaxSize()) {
        drawRect(brush = grain, alpha = 0.45f)
        val radius = size.maxDimension * 0.62f
        drawRect(
            brush = Brush.radialGradient(
                0.55f to Color.Transparent,
                1f to Color.Black.copy(alpha = rim),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = radius,
            )
        )
    }
}
