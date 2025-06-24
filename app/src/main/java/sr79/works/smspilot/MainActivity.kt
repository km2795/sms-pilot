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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import sr79.works.smspilot.composables.LandingPage
import java.nio.MappedByteBuffer


const val APP_TITLE = "SMS Pilot"
var DETECTOR: MappedByteBuffer? = null

class MainActivity : ComponentActivity() {

  // ViewModel for managing the SMS_LIST
  private val smsViewModel: SmsViewModel by viewModels()

  // For controlling visibility of the permission button.
  private var showPermissionButton by mutableStateOf(true)

  val onShowPermissionButton = { show: Boolean ->
    showPermissionButton = show
    if (show) {
      smsViewModel.clearSmsMessages()
      SmsPilot().unLoadSmsList()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val loadPermission = DataStore().getSmsReadPermission(this)

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
      smsViewModel.initialLoadSmsMessages(this.contentResolver)
    }

    /*
     * Load the detector.
     */
    DETECTOR = SmsPilot().setupDetector(this)

    setContent {
      // Collect the SMS list from the ViewModel as a State.
      val smsListFromViewModel by smsViewModel.smsThreads.collectAsState()

      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LandingPage(
          SmsPilot().formAndGetThreadList(smsListFromViewModel.toMutableList()),
          DETECTOR,
          showPermissionButton,
          onShowPermissionButton,
          smsViewModel,
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}
