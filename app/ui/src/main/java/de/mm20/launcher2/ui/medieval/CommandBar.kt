package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.ui.launcher.search.SearchVM

/**
 * "Command the Oracle..." on Home. A tap opens the command overlay (CommandOverlay.kt), where the
 * bar is a real text field with the keyboard up and the search results below it. SUMMON hands the
 * text in `SearchVM.searchQuery` to the Claude app (see Summon.kt); with no text it only shows the hint.
 */
@Composable
internal fun CommandBar(onOpen: () -> Unit) {
    val context = LocalContext.current
    val searchVM: SearchVM = viewModel()
    val land = LocalLandscape.current
    Panel(top = 404f, height = 44f, onClick = onOpen) {
        SummonButton(Modifier.align(Alignment.TopEnd).padding(top = 7.dp, end = 8.dp)) {
            summonToClaude(context, searchVM.searchQuery.value)
        }
        SvgIcon(MedievalIcons.Quill, if (land) 17f else 20f, Medieval.Gold, Modifier.at(if (land) 11f else 16f, if (land) 13f else 12f))
        MText(
            "Command the Oracle…", if (land) 11.5f else 16f, Medieval.TextFaint,
            Modifier.at(if (land) 34f else 46f, if (land) 15f else 11f).let { if (land) it.width(98.dp) else it }, italic = true,
        )
    }
}
