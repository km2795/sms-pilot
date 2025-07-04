package sr79.works.smspilot.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import sr79.works.smspilot.APP

@Composable
fun TopBarActions(
  showMenu: Boolean,
  onShowExtraTopActionMenu: (Boolean) -> Unit,
  onShowPermissionButton: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  // Load the context.
  val context = LocalContext.current

  IconButton(onClick = { onShowExtraTopActionMenu(true) }) {
    Icon(
      imageVector = Icons.Filled.MoreVert,
      tint = Color.White,
      contentDescription = "More Options",
    )
  }
  DropdownMenu(
    expanded = showMenu,
    onDismissRequest = { onShowExtraTopActionMenu(false) }
  ) {
    DropdownMenuItem(
      text = { Text("Clear Permissions") },
      onClick = {
        // Update the "READ NO" permission to the data store.
        APP.DATA_STORE_HANDLE?.updateSmsReadPermission(context, false)

        // Update the ThreadList Composable to hide the list and show the button.
        onShowPermissionButton(true)

        // Hide the extra actions menu.
        onShowExtraTopActionMenu(false)
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Filled.Clear,
          contentDescription = "Clear Permissions Icon",
          tint = Color.Black
        )
      }
    )
  }
}