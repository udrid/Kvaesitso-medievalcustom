package de.mm20.launcher2.ui.settings.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.locals.LocalBackStack
import de.mm20.launcher2.ui.medieval.MedievalCornerPreference
import de.mm20.launcher2.ui.settings.transparencies.TransparencySchemesSettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object AppearanceSettingsRoute: NavKey

@Composable
fun AppearanceSettingsScreen() {
    val viewModel: AppearanceSettingsScreenVM = viewModel()
    val backStack = LocalBackStack.current
    val transparencyThemeName by viewModel.transparencyThemeName.collectAsStateWithLifecycle(null)

    PreferenceScreen(title = stringResource(id = R.string.preference_screen_appearance)) {
        // Deliverance (F1, T20, owner picks): the light/dark, colour, typography and shape pickers, Theme
        // import/export and Colour source are hidden because LauncherTheme uses the fixed Deliverance look;
        // the colour scheme is in the main settings. Corner style (buttons and chips) is new.
        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(id = R.string.preference_screen_transparencies),
                    summary = transparencyThemeName,
                    onClick = {
                        backStack.add(TransparencySchemesSettingsRoute)
                    },
                    icon = R.drawable.opacity_24px,
                )
                MedievalCornerPreference()
            }
        }
    }
}
