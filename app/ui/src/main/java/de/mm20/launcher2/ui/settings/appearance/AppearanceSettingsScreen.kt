package de.mm20.launcher2.ui.settings.appearance

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.ktx.isAtLeastApiLevel
import de.mm20.launcher2.ui.R
import de.mm20.launcher2.ui.component.preferences.ListPreference
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.locals.LocalBackStack
import de.mm20.launcher2.ui.locals.LocalBackStack
import de.mm20.launcher2.ui.settings.transparencies.TransparencySchemesSettingsRoute
import kotlinx.serialization.Serializable

@Serializable
data object AppearanceSettingsRoute: NavKey

@Composable
fun AppearanceSettingsScreen() {
    val viewModel: AppearanceSettingsScreenVM = viewModel()
    val backStack = LocalBackStack.current
    val transparencyThemeName by viewModel.transparencyThemeName.collectAsStateWithLifecycle(null)
    val compatModeColors by viewModel.compatModeColors.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it == null) {
            return@rememberLauncherForActivityResult
        }
        backStack.add(ImportThemeSettingsRoute(it))
    }

    PreferenceScreen(title = stringResource(id = R.string.preference_screen_appearance)) {
        // Deliverance (F1, owner pick): the light/dark, colour, typography and shape pickers are hidden because
        // LauncherTheme uses the fixed Deliverance look; the colour scheme is in the main settings.
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
            }
        }

        item {
            PreferenceCategory {
                Preference(
                    title = stringResource(R.string.theme_import_title),
                    icon = R.drawable.arrow_circle_down_24px,
                    onClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    }
                )
                Preference(
                    title = stringResource(R.string.theme_export_title),
                    icon = R.drawable.arrow_circle_up_24px,
                    onClick = {
                        backStack.add(ExportThemeSettingsRoute)
                    }
                )
            }
        }

        if (isAtLeastApiLevel(31)) {
            item {
                PreferenceCategory(stringResource(R.string.preference_category_advanced)) {
                    ListPreference(
                        title = stringResource(R.string.preference_mdy_color_source),
                        items = listOf(
                            stringResource(R.string.preference_mdy_color_source_system) to false,
                            stringResource(R.string.preference_mdy_color_source_wallpaper) to true,
                        ),
                        value = compatModeColors,
                        onValueChanged = {
                            viewModel.setCompatModeColors(it)
                        }
                    )
                }
            }
        }
    }
}