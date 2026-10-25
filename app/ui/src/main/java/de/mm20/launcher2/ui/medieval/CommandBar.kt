package de.mm20.launcher2.ui.medieval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import kotlinx.coroutines.launch

/**
 * "Command the Oracle..." is the launcher's own search (apps, contacts, calendar, settings, web).
 * SUMMON is left out: no verified way yet to hand text to the Claude app (see handoff).
 */
@Composable
internal fun CommandBar(state: LauncherScaffoldState) {
    val scope = rememberCoroutineScope()
    Panel(top = 554f, height = 44f, onClick = { scope.launch { state.onSearchBarTap() } }) {
        SvgIcon(MedievalIcons.Quill, 20f, Medieval.Gold, Modifier.at(16f, 12f))
        MText(
            "Command the Oracle…", 16f, Medieval.TextFaint,
            Modifier.at(46f, 11f), italic = true,
        )
    }
}
