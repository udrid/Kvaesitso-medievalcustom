package de.mm20.launcher2.ui.launcher.search.common.list

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import de.mm20.launcher2.ui.medieval.Ribbon
import de.mm20.launcher2.ui.medieval.medievalFrame
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.ui.ktx.animateShapeAsState
import de.mm20.launcher2.ui.layout.BottomReversed
import de.mm20.launcher2.ui.theme.transparency.transparency

fun <T : SavableSearchable> LazyListScope.ListResults(
    key: String,
    items: List<T>,
    itemContent: @Composable ColumnScope.(T, Boolean, Int) -> Unit,
    before: @Composable (ColumnScope.() -> Unit)? = null,
    after: @Composable (ColumnScope.() -> Unit)? = null,
    reverse: Boolean = false,
    selectedIndex: Int = -1,
    title: String? = null,
) {
    if (before != null) {
        item(
            key = "$key-before",
            contentType = { "$key-before" },
        ) {
            ListItemSurface(
                isFirst = true,
                isLast = after == null && items.isEmpty(),
                reverse = reverse,
                isBeforeExpanded = selectedIndex == 0,
                ribbon = title,
            ) {
                before()
            }
        }
    }
    val rows = items.size
    items(
        items.size,
        key = {
            "$key-${items[it].key}"
        },
        contentType = { key },
    ) {
        val item = items[it]
        val showDetails = it == selectedIndex

        ListItemSurface(
            isFirst = it == 0 && before == null,
            isLast = it == rows - 1 && after == null,
            reverse = reverse,
            isExpanded = showDetails,
            isBeforeExpanded = selectedIndex - 1 == it,
            isAfterExpanded = selectedIndex >= 0 && selectedIndex + 1 == it,
            ribbon = title,
        ) {
            itemContent(item, showDetails, it)
        }
    }
    if (after != null) {
        item(
            key = "$key-after",
            contentType = { "$key-after" },
        ) {
            ListItemSurface(
                isFirst = before == null && items.isEmpty(),
                isLast = true,
                reverse = reverse,
                isAfterExpanded = selectedIndex == items.lastIndex,
                ribbon = title,
            ) {
                after()
            }
        }
    }
}

@Composable
fun LazyItemScope.ListItemSurface(
    isFirst: Boolean = false,
    isLast: Boolean = false,
    reverse: Boolean = false,
    isExpanded: Boolean = false,
    isBeforeExpanded: Boolean = false,
    isAfterExpanded: Boolean = false,
    ribbon: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val transition = updateTransition(isExpanded)
    val backgroundAlpha by transition.animateFloat {
        if (it) MaterialTheme.transparency.elevatedSurface else MaterialTheme.transparency.surface
    }
    val elevation by transition.animateDp {
        if (it && backgroundAlpha == 1f) 2.dp else 0.dp
    }


    val padding by transition.animateDp {
        if (it) 8.dp else 1.dp
    }

    val shape by animateShapeAsState(
        if (isExpanded) MaterialTheme.shapes.medium
        else if (!isFirst && !isLast && !isAfterExpanded && !isBeforeExpanded) MaterialTheme.shapes.extraSmall
        else {
            val xs = MaterialTheme.shapes.extraSmall
            val md = MaterialTheme.shapes.medium
            if (reverse) {
                xs.copy(
                    topStart = if (isLast || isBeforeExpanded) md.topStart else xs.topStart,
                    topEnd = if (isLast || isBeforeExpanded) md.topEnd else xs.topEnd,
                    bottomEnd = if (isFirst || isAfterExpanded) md.bottomEnd else xs.bottomEnd,
                    bottomStart = if (isFirst || isAfterExpanded) md.bottomStart else xs.bottomStart,
                )
            } else {
                xs.copy(
                    topStart = if (isFirst || isAfterExpanded) md.topStart else xs.topStart,
                    topEnd = if (isFirst || isAfterExpanded) md.topEnd else xs.topEnd,
                    bottomEnd = if (isLast || isBeforeExpanded) md.bottomEnd else xs.bottomEnd,
                    bottomStart = if (isLast || isBeforeExpanded) md.bottomStart else xs.bottomStart,
                )
            }
        }
    )

    // Deliverance: the padding between items is covered by the frame's side lines, so a group of
    // items reads as one framed panel (see medievalFrame).
    val topPadding = if (reverse) { if (!isLast) padding else 12.dp } else { if (!isFirst) padding else 0.dp }
    val bottomPadding = if (reverse) { if (!isFirst) padding else 0.dp } else { if (!isLast) padding else 12.dp }
    val topLine = isExpanded || if (!reverse) isFirst || isAfterExpanded else isLast || isBeforeExpanded
    val bottomLine = isExpanded || if (!reverse) isLast || isBeforeExpanded else isFirst || isAfterExpanded
    val showRibbon = ribbon != null && if (reverse) isLast else isFirst

    Box(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .medievalFrame(
                top = topLine,
                bottom = bottomLine,
                topInset = topPadding,
                bottomInset = bottomPadding,
            )
            .padding(top = topPadding, bottom = bottomPadding)
            .shadow(
                elevation = elevation,
                shape,
                true,
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(backgroundAlpha)),
            verticalArrangement = if (reverse) Arrangement.BottomReversed else Arrangement.Top
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                if (showRibbon && !reverse) Spacer(Modifier.height(10.dp))
                content()
                if (showRibbon && reverse) Spacer(Modifier.height(10.dp))
            }
        }
        if (showRibbon) {
            Ribbon(
                ribbon!!,
                Modifier.align(Alignment.TopStart).offset(x = 12.dp, y = (-8).dp),
            )
        }
    }
}
