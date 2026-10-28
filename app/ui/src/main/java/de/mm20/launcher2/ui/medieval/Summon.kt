package de.mm20.launcher2.ui.medieval

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.ui.launcher.search.SearchVM

/**
 * SUMMON (owner decision 2026-10-26, route C): hand the typed text to the Claude app as a share
 * (ACTION_SEND text/plain, package [ClaudePackage]) AND copy it to the clipboard at the same moment.
 * The owner taps Send in the Claude app; nothing is sent automatically.
 * UNVERIFIED on the phone: whether the app opens a new chat or adds the text to the open one.
 */
internal fun summonToClaude(context: Context, text: String) {
    val question = text.trim()
    if (question.isEmpty()) {
        Toast.makeText(context, "Write thy question first.", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Question for the Oracle", question))
    } catch (e: Exception) {
        Log.e("medieval", "SUMMON: could not copy the question to the clipboard", e)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, question)
        setPackage(ClaudePackage)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
        Toast.makeText(context, "Copied. Paste if needed.", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        Log.e("medieval", "SUMMON: the Claude app ($ClaudePackage) does not take shared text", e)
        Toast.makeText(context, "The Claude app is not installed. Question copied.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Log.e("medieval", "SUMMON: could not open the Claude app", e)
        Toast.makeText(context, "Could not open the Claude app. Question copied.", Toast.LENGTH_LONG).show()
    }
}

/** The C5 SUMMON button: dark red plate, gold hairline, Cinzel 10 bold, tracking .18em. */
@Composable
internal fun SummonButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier
            .height(30.dp)
            .background(Brush.verticalGradient(listOf(Medieval.ShieldTop, Medieval.SummonDark)), shape)
            .border(1.dp, Medieval.GoldDark, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "SUMMON",
            color = Medieval.Text,
            fontFamily = Medieval.Cinzel,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.18.em,
        )
    }
}

/**
 * SUMMON inside the launcher's own search bar (menu slot, shown while there is text), so a question
 * typed in the search can be summoned. The search itself is untouched.
 */
@Composable
fun SummonSearchBarButton() {
    val context = LocalContext.current
    val searchVM: SearchVM = viewModel()
    val query = searchVM.searchQuery.value
    if (query.isBlank()) return
    SummonButton(Modifier.padding(horizontal = 4.dp)) { summonToClaude(context, query) }
}
