package de.mm20.launcher2.ui.medieval

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.ui.launcher.search.SearchColumn
import de.mm20.launcher2.ui.launcher.search.SearchVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * T13: the command bar as a real text field. Covers Home (the bar rises to the top so the keyboard
 * cannot hide it); the launcher's own search results show below it. The text is
 * `SearchVM.searchQuery`, the same string the search page uses. Nothing is listed before typing.
 */
@Composable
internal fun CommandOverlay(onClose: () -> Unit) {
    val context = LocalContext.current
    val searchVM: SearchVM = viewModel()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    var keepOnStop by remember { mutableStateOf(false) }
    val query = searchVM.searchQuery.value

    fun close() {
        searchVM.reset()
        focusManager.clearFocus()
        onClose()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }
    // Leaving the launcher: opening an app from a result clears the field; SUMMON keeps the text
    // so the question is still there when the owner comes back.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (keepOnStop) keepOnStop = false else close()
    }
    BackHandler { close() }
    HomePressed.Listen { close() }

    val density = LocalDensity.current
    val topInset = with(density) { WindowInsets.statusBarsIgnoringVisibility.getTop(this).toDp() }
    val bottomInsets = WindowInsets.ime.union(WindowInsets.navigationBars).asPaddingValues()

    Box(
        Modifier
            .fillMaxSize()
            .background(Medieval.Ink)
            .pointerInput(Unit) {}
            .windowInsetsPadding(WindowInsets.displayCutout.union(WindowInsets.navigationBars).only(WindowInsetsSides.Horizontal))
            .padding(top = topInset + 8.dp),
    ) {
        val shape = RoundedCornerShape(2.dp)
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(48.dp)
                .background(Brush.verticalGradient(listOf(Medieval.PanelTop, Medieval.PanelBottom)), shape)
                .border(1.dp, Medieval.GoldDark, shape)
                .padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SvgIcon(MedievalIcons.Quill, 20f, Medieval.Gold)
            Box(Modifier.weight(1f).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        "Command the Oracle…", color = Medieval.TextFaint, fontSize = 16.sp,
                        fontFamily = Medieval.FellEnglish, fontStyle = FontStyle.Italic, maxLines = 1,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { searchVM.search(it) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Medieval.TextBright, fontSize = 16.sp, fontFamily = Medieval.FellEnglish,
                    ),
                    cursorBrush = SolidColor(Medieval.Gold),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { searchVM.launchBestMatchOrAction(context) }),
                )
            }
            if (query.isNotEmpty()) {
                Text(
                    "✕", color = Medieval.TextDim, fontSize = 16.sp,
                    modifier = Modifier
                        .clickable { searchVM.search("") }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
            SummonButton {
                keepOnStop = true
                scope.launch { delay(2000); keepOnStop = false }
                summonToClaude(context, searchVM.searchQuery.value)
            }
        }
        if (query.isNotEmpty()) {
            SearchColumn(
                modifier = Modifier.fillMaxSize().padding(top = 56.dp),
                paddingValues = PaddingValues(
                    top = 4.dp,
                    bottom = bottomInsets.calculateBottomPadding() + 8.dp,
                ),
                onHideKeyboard = { keyboard?.hide() },
            )
        } else {
            CommandLayerContent(Modifier.fillMaxSize().padding(top = 56.dp, bottom = bottomInsets.calculateBottomPadding()))
        }
    }
}
