package sr79.works.smspilot.composables

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import sr79.works.smspilot.AppHandler
import sr79.works.smspilot.DataStore
import sr79.works.smspilot.LandingPageViewModel
import sr79.works.smspilot.Thread
import java.nio.MappedByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingPage(
  appTitle: String,
  smsList: List<Thread>,
  dataStore: DataStore,
  modelFile: MappedByteBuffer?,
  showPermissionButton: Boolean,
  onShowPermissionButton: (Boolean) -> Unit,
  landingPageViewModel: LandingPageViewModel,
  modifier: Modifier = Modifier
) {

  val context = LocalContext.current
  
  // For controlling visibility of the extra top bar actions.
  var showExtraTopActionMenu by rememberSaveable { mutableStateOf(false) }

  // ActivityResultLauncher for permission request
  val requestPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        onShowPermissionButton(false)

        // Load through the ViewModel.
        landingPageViewModel.loadSmsMessages()

        // Update the "READ YES" permission to the data store.
        dataStore.updateSmsReadPermission(context, true)
      } else {
        // User denied permission.
        onShowPermissionButton(true)

        // Update the "READ NO" permission to the data store.
        AppHandler.updateSmsReadPermission(dataStore, context, false)

        // Not essentially required.
        landingPageViewModel.clearSmsMessages()
      }
    }

  val onRequestPermission = {
    when {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
      ) == PackageManager.PERMISSION_GRANTED -> {
        // Update the "READ YES" permission to the data store.
        AppHandler.updateSmsReadPermission(dataStore, context, true)

        // Load through ViewModel.
        landingPageViewModel.loadSmsMessages()

        // Hide the permission button.
        onShowPermissionButton(false)
      }
      else -> {
        // Update the "READ NO" permission to the data store.
        AppHandler.updateSmsReadPermission(dataStore, context, false)

        // Show the permission dialog.
        requestPermissionLauncher.launch(Manifest.permission.READ_SMS)

        // Show the permission button.
        onShowPermissionButton(true)

        // Not needed essentially.
        landingPageViewModel.clearSmsMessages()
      }
    }
  }

  Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    TopAppBar(
      title = { Text(appTitle) },
      navigationIcon = {},
      actions = {
        TopBarActions(
          showMenu = showExtraTopActionMenu,
          dataStore,
          onShowExtraTopActionMenu = { showExtraTopActionMenu = it },
          onShowPermissionButton,
          modifier = modifier
        )
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.hsv(270f, 0.4f, 0.3f),
        titleContentColor = Color.hsv(0f, 0f, 1f)
      ),
      modifier = Modifier.shadow(6.dp)
    )
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.White),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (showPermissionButton) {
        Button(onClick = onRequestPermission) {
          Text("Load SMS Messages")
        }
        if (smsList.isEmpty()) {
          Text(
            "No SMS messages found or permission not granted.",
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        ThreadList(
          smsList,
          modelFile,
          modifier = Modifier
        )
      }
    }
  }
}
