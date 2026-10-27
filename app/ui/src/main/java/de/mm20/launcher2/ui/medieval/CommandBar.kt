package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.layout.padding
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
    Panel(top = 404f, height = 44f, onClick = onOpen) {
        SummonButton(Modifier.align(Alignment.TopEnd).padding(top = 7.dp, end = 8.dp)) {
            summonToClaude(context, searchVM.searchQuery.value)
        }
        SvgIcon(MedievalIcons.Quill, 20f, Medieval.Gold, Modifier.at(16f, 12f))
        MText(
            "Command the Oracle…", 16f, Medieval.TextFaint,
            Modifier.at(46f, 11f), italic = true,
        )
    }
}
