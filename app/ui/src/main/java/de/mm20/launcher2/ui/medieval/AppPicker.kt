package de.mm20.launcher2.ui.medieval

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Number of columns in the picker: 4 (5 columns would be ~67 dp wide on a 390 dp screen, too tight for names). */
private const val PickerColumns = 4

@Composable
private fun AppCell(app: AppEntry, onPick: (AppEntry) -> Unit) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        try {
            drawableToBitmap(context.packageManager.getApplicationIcon(app.packageName), 96).asImageBitmap()
        } catch (e: Exception) {
            Log.e("medieval", "No icon for ${app.packageName}", e)
            null
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onPick(app) }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Image(icon, null, Modifier.size(44.dp))
        } else {
            Box(Modifier.size(44.dp).border(1.dp, Medieval.GoldDark))
        }
        MText(
            app.label.uppercase(), 8.5f, Medieval.Text,
            Modifier.padding(top = 5.dp).fillMaxWidth(),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.08f,
            align = TextAlign.Center, maxLines = 2,
        )
    }
}

/** Dark vellum app picker: search bar on top, scrolling grid of icon above name. */
@Composable
internal fun AppPickerDialog(apps: List<AppEntry>, onDismiss: () -> Unit, onPick: (AppEntry) -> Unit) {
    var query by remember { mutableStateOf("") }
    val shown = remember(query, apps) {
        val q = query.trim()
        if (q.isEmpty()) apps else apps.filter { it.label.contains(q, ignoreCase = true) }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .fillMaxWidth(0.94f)
                .height(560.dp)
                .background(Brush.verticalGradient(listOf(Medieval.PanelTop, Medieval.PanelBottom)))
                .border(1.dp, Medieval.GoldDark)
                .padding(14.dp)
        ) {
            Cap("Choose the app for this shield", Modifier.padding(bottom = 10.dp), size = 11f)
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(1.dp, Medieval.GoldDark)
                    .background(androidx.compose.ui.graphics.Color(0xFF050505))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                if (query.isEmpty()) {
                    MText("Search apps...", 15f, Medieval.TextFaint, italic = true)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Medieval.Text,
                        fontSize = 15.sp,
                        fontFamily = Medieval.FellEnglish,
                    ),
                    cursorBrush = SolidColor(Medieval.Gold),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            LazyVerticalGrid(columns = GridCells.Fixed(PickerColumns)) {
                items(shown, key = { it.packageName }) { app -> AppCell(app, onPick) }
            }
        }
    }
}
