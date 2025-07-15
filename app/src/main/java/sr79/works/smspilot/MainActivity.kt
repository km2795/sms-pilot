package sr79.works.smspilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import sr79.works.smspilot.composables.LandingPage
import java.nio.MappedByteBuffer


// Landing Page.
class MainActivity : ComponentActivity() {

  // App's title.
  private val appTitle = "SMS Pilot"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

     //Load the detector.
    val detector: MappedByteBuffer? = AppHandler.setupDetector(this)

    // Load the Database and Data store handle.
    val dataStore: DataStore = DataStore(this)

    // ViewModel for Landing Page.
    val landingPageViewModel by viewModels<LandingPageViewModel> {
      LandingPageViewModel.LandingPageViewModelFactory(application, dataStore, detector)
    }


    setContent {
      val displayThreads by landingPageViewModel.displayThreads.collectAsState()
      val showPermissionButton by landingPageViewModel.showPermissionButton.collectAsState()

      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LandingPage(
          appTitle,
          displayThreads,
          dataStore,
          showPermissionButton,
          updatePermissionButtonVisibility = { show -> landingPageViewModel.updateShowPermissionButton(show) },
          landingPageViewModel,
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}
