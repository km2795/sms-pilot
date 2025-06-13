package com.example.smspilot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.nio.MappedByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val APP_TITLE = "SMS Pilot"
var DETECTOR: MappedByteBuffer? = null

class MainActivity : ComponentActivity() {
  
  private var smsListState by mutableStateOf<List<Thread>>(emptyList())

  // ActivityResultLauncher for permission request
  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        // Load the SMSs.
        smsListState = SmsPilot().fetchSmsMessages(contentResolver)
      } else {
        // In case user denies permission.
      }
    }
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    /*
     * Load the detector.
     */
    DETECTOR = SmsPilot().setupDetector(this)

    setContent {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        MainScreen(
          smsList = smsListState,
          DETECTOR,
          onRequestPermission = {
            when {
              ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
              ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                smsListState = SmsPilot().fetchSmsMessages(contentResolver)
              }
              
              shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) -> {
                // Request for permission for reading SMSs.
                requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
              }
              
              else -> {
                // You can directly ask for the permission.
                requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
              }
            }
          },
          modifier = Modifier.padding(innerPadding))
      }
    }
  }
}

// Simple Composable to display the SMS list and a button to request permission
@Composable
fun ThreadList(smsList: List<Thread>,
               modelFile: MappedByteBuffer?,
               onRequestPermission: () -> Unit,
               modifier: Modifier = Modifier) {

  Column(modifier = modifier) {
    Button(onClick = onRequestPermission) {
      Text("Load SMS Messages")
    }
    if (smsList.isEmpty()) {
      Text("No SMS messages found or permission not granted.")
    } else {
      smsList.forEach { sms ->
        val smsBody: String = sms.getBodyThumbnail()
        /*val verdict: String = runInference(modelFile, smsBody)*/

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
          Text("From: ${sms.getAddress()}")
          Text("Message: ${smsBody}")
          Text("Listing Size: ${sms.getThreadSize()}")
          Text(
            "Date: ${
              SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
              ).format(Date(sms.getShowDate()))
            }"
          )
          // Show the verdict of the message. (Spam or not).
          /*Text(verdict, fontWeight = FontWeight.Bold)*/
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  smsList: List<Thread>,
  modelFile: MappedByteBuffer?,
  onRequestPermission: () -> Unit,
  modifier: Modifier = Modifier) {

  // Scroll option.
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(APP_TITLE) },
        navigationIcon = {},
        actions = {
          IconButton(onClick = {}) {
            Icon(
              imageVector = Icons.Filled.MoreVert,
              contentDescription = "Navigation Menu",
            )
          }
          IconButton(onClick = {}) {}
          IconButton(onClick = {}) {}
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary,
        ),

        // Enable the scrolling behavior.
         scrollBehavior = scrollBehavior
      )
    }
  ) { innerPadding ->
    ThreadList(
      smsList,
      modelFile,
      onRequestPermission,
      modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState()))
  }
}
