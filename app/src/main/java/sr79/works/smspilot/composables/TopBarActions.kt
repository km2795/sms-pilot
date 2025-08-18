package sr79.works.smspilot.composables

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import sr79.works.smspilot.API_URL
import sr79.works.smspilot.DataStore

@Composable
fun TopBarActions(
  showMenu: Boolean,
  dataStore: DataStore,
  onShowExtraTopActionMenu: (Boolean) -> Unit,
  onShowPermissionButton: (Boolean) -> Unit,
  runPredictor: suspend () -> Unit,
  modifier: Modifier = Modifier
) {
  // Load the context.
  val context = LocalContext.current
  var showUpdateApiUrlDialog by remember { mutableStateOf(false) }
  var scope = rememberCoroutineScope()

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
        dataStore.updateSmsReadPermission(context, false)

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

    // Drop down for Updating the detector API URL.
    DropdownMenuItem(
      text = { Text("Update Detector API URL ") },
      onClick = {
        showUpdateApiUrlDialog = true
        onShowExtraTopActionMenu(false)
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = "Update API URL Icon"
        )
      }
    )
  }

  if (showUpdateApiUrlDialog) {
    DetectorApiUrlUpdateDialog(
      showDialog = showUpdateApiUrlDialog,
      onDismissRequest = {
        showUpdateApiUrlDialog = false
      },
      currentUrl = "http://127.0.0.1:5000/predict",
      onSave = { newUrl ->
        API_URL = newUrl
        showUpdateApiUrlDialog = false

        // To kick start the predictor.
        scope.launch {
          try {
            runPredictor()
            Log.d("TopBarActions", "runPredictor() was invoked successfully.")
          } catch (e: Exception) {
            Log.e("TopBarActions", "Error calling runPredictor()", e)
          }
        }

        Log.d("showUpdateApiUrlDialog", "Spam Detector API URL Updated: " + API_URL)
      }
    )
  }
}