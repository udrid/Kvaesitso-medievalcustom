package de.mm20.launcher2.ui.medieval

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.permissions.PermissionGroup

/** Dispatches: the phone's active notifications, newest first. */
@Composable
internal fun Dispatches(vm: MedievalHomeVM) {
    val context = LocalContext.current
    val hasAccess by vm.hasNotificationAccess.collectAsState()

    Panel(top = 612f, height = 90f, ribbon = "Dispatches") {
        Column(
            Modifier.at(16f, 27f),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            if (hasAccess == false) {
                Row(
                    Modifier.clickable {
                        (context as? AppCompatActivity)?.let {
                            vm.permissionsManager.requestPermission(it, PermissionGroup.Notifications)
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MText("Tap to grant notification access", 13f, Medieval.TextSoft)
                }
            }
        }
    }
}
