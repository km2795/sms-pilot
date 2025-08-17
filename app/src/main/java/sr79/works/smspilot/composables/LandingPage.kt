package sr79.works.smspilot.composables

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import sr79.works.smspilot.DataStore
import sr79.works.smspilot.LandingPageViewModel
import sr79.works.smspilot.ui.theme.OrangeDef
import java.nio.MappedByteBuffer

/**
 * First or Main screen of the App.
 *
 * @param appTitle App's title.
 * @param dataStore App's data store (handler functions).
 * @param detector App's detector model.
 * @param showPermissionButton Flag for visibility of Permission Button.
 * @param updatePermissionButtonVisibility callback for updating permission button's visibility.
 * @param landingPageViewModel [LandingPageViewModel] to handle logic related to the landing page.
 * @param modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingPage(
  appTitle: String,
  dataStore: DataStore,
  detector: MappedByteBuffer?,
  showPermissionButton: Boolean,
  updatePermissionButtonVisibility: (Boolean) -> Unit,
  landingPageViewModel: LandingPageViewModel = viewModel(
    factory = LandingPageViewModel.LandingPageViewModelFactory(
      application = LocalContext.current.applicationContext as Application,
      dataStore = dataStore,
      detector = detector
    )
  ),
  modifier: Modifier = Modifier
) {

  // For controlling visibility of the extra top bar actions.
  var showExtraTopActionMenu by rememberSaveable { mutableStateOf(false) }

  val displayThreads = landingPageViewModel.displayThreads.collectAsState()

  val requestPermissionLauncher =
    rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
      landingPageViewModel.handlePermissionResult(isGranted)
    }

  val onRequestPermissionClick = {
    landingPageViewModel.checkAndRequestPermission(requestPermissionLauncher)
  }

  Column(
    modifier = Modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    TopAppBar(
      title = { Text(appTitle) },
      navigationIcon = {},
      actions = {
        TopBarActions(
          showMenu = showExtraTopActionMenu,
          dataStore,
          onShowExtraTopActionMenu = { showExtraTopActionMenu = it },
          updatePermissionButtonVisibility,
          modifier = Modifier.background(Color.White)
        )
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = OrangeDef,
        titleContentColor = Color.White
      ),
      modifier = Modifier.shadow(10.dp)
    )
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.White),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (showPermissionButton) {
        ElevatedButton(
          onClick = onRequestPermissionClick,
          colors =  ButtonColors(
            contentColor = Color.White,
            disabledContainerColor = OrangeDef,
            disabledContentColor = OrangeDef,
            containerColor = OrangeDef
          )) {
          Text("Load SMS Messages")
        }
        Text(
          "No SMS messages found or permission not granted.",
          modifier = Modifier.padding(16.dp)
        )
      } else {
        ThreadList(displayThreads.value)
      }
    }
  }
}
