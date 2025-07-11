﻿package sr79.works.smspilot

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
import sr79.works.smspilot.APP.DATA_STORE_HANDLE
import sr79.works.smspilot.APP.detector
import sr79.works.smspilot.composables.LandingPage
import java.nio.MappedByteBuffer


// Globals.
object APP {
  // Title of the Top Action Bar (in the main activity).
  const val APP_TITLE = "SMS Pilot"

  // Reference to the detector.
  var detector: MappedByteBuffer? = null

  // Handle for data store.
  var DATA_STORE_HANDLE: DataStore? = null
}

// Landing Page.
class MainActivity : ComponentActivity() {

  // ViewModel for managing the Landing Page (or Landing Screen).
  private val landingPageViewModel: LandingPageViewModel by viewModels()

  // For controlling visibility of the permission button.
  private var showPermissionButton by mutableStateOf(true)

  val onShowPermissionButton = { show: Boolean ->
    showPermissionButton = show
    if (show) {
      landingPageViewModel.clearSmsMessages()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    /*
     * Load the detector.
     */
    detector = AppHandler.setupDetector(this)

    /*
     * Load the Database and Data store handle.
     */
    DATA_STORE_HANDLE = DataStore(this)

    val loadPermission = AppHandler.checkSmsReadPermission(this)

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
      // Collect the data objects from the ViewModel as states.
      val messageList by landingPageViewModel.messageList.collectAsState()
      val threadList by landingPageViewModel.threadList.collectAsState()
      val threadListMap by landingPageViewModel.threadListMap.collectAsState()

      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LandingPage(
          APP.APP_TITLE,
          AppHandler.formAndGetThreadList(
            threadListMap.toMutableMap(),
            messageList.toMutableList()
          ),
          detector,
          showPermissionButton,
          onShowPermissionButton,
          landingPageViewModel,
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}
