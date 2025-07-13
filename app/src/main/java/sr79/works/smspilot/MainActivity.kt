package sr79.works.smspilot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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

    //Load the permission from the data store.
    val loadPermission = AppHandler.checkSmsReadPermission(dataStore,this)

    // ViewModel for managing the Landing Page (or Landing Screen).
    val landingPageViewModel by viewModels<LandingPageViewModel> {
      LandingPageViewModel(application, dataStore, detector)
    }

    // For controlling visibility of the permission button.
    var showPermissionButton by mutableStateOf(true)

    val onShowPermissionButton = { show: Boolean ->
      showPermissionButton = show
      if (show) {
        landingPageViewModel.clearSmsMessages()
      }
    }

    /*
     * In case user disables the permission or the system revokes the
     * permission outside the app. This would potentially render the
     * app to crash. Adding a check at the start to see if the permission
     * in the data store is true and system-wide permission is false.
     * If so, re-orient the app to the permission screen.
     */
    if (
      loadPermission
      &&
      (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_SMS
      ) != PackageManager.PERMISSION_GRANTED)) {

      // Hide the permission button.
      onShowPermissionButton(true)

    } else if (loadPermission) {
      onShowPermissionButton(false)

      // Load through ViewModel.
      landingPageViewModel.loadSmsMessages()
    }

    setContent {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LandingPage(
          appTitle,
          AppHandler.getThreadList(detector, this.contentResolver),
          dataStore,
          showPermissionButton,
          onShowPermissionButton,
          landingPageViewModel,
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}
