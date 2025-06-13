package com.example.smspilot

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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


class MainActivity : ComponentActivity() {
  
  private var smsListState by mutableStateOf<List<SmsMessage>>(emptyList())
  
  // ActivityResultLauncher for permission request
  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        // Permission is granted. Continue the action or workflow in your
        // app.
        smsListState = fetchSmsMessages()
      } else {
        // Explain to the user that the feature is unavailable because the
        // features requires a permission that the user has denied. At the
        // same time, respect the user's decision. Don't link to system
        // settings in an effort to convince the user to change their
        // decision.
        // You can show a Snackbar or a Dialog here.
      }
    }
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Load the model from the storage.
    val detectorModel : MappedByteBuffer? =
      loadModelFile(this, "sms_spam_detector_model.tflite")

    val vectorizer = HashingVectorizer(nFeatures = 200) // Or your desired number of features
    val myText = """
      This is the first sentence.
      Here is another sentence for processing.
      Word2Vec is interesting.
    """.trimIndent() // Example multi-line string

    val wordVector = vectorizer.transform(listOf(myText))
    println("Word Vector: ${wordVector.contentToString()}")

    setContent {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        SmsScreen(
          smsList = smsListState,
          detectorModel,
          onRequestPermission = {
            when {
              ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
              ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                smsListState = fetchSmsMessages()
              }
              
              shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                // For brevity, we'll just request it here.
                requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
              }
              
              else -> {
                // You can directly ask for the permission.
                requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
              }
            }
          },
          modifier = Modifier.padding(innerPadding).verticalScroll(rememberScrollState())
        )
      }
    }
  }
  
  private fun fetchSmsMessages(): List<SmsMessage> {
    val smsList = mutableListOf<SmsMessage>()
    val contentResolver = contentResolver
    val uri = Telephony.Sms.Inbox.CONTENT_URI // Or Telephony.Sms.Sent.CONTENT_URI for sent, etc.
    
    // Columns to retrieve
    val projection = arrayOf(
      Telephony.Sms._ID,
      Telephony.Sms.ADDRESS, // Sender's phone number
      Telephony.Sms.BODY,    // Message body
      Telephony.Sms.DATE     // Date the message was received
      // Add other columns if needed, e.g., Telephony.Sms.TYPE
    )
    
    // You can add a selection and selectionArgs to filter messages
    // For example, to get messages from a specific number:
    // val selection = "${Telephony.Sms.ADDRESS} = ?"
    // val selectionArgs = arrayOf("1234567890")
    // Or to get messages after a certain date:
    // val selection = "${Telephony.Sms.DATE} > ?"
    // val selectionArgs = arrayOf(specificTimestamp.toString())
    
    val cursor: Cursor? = contentResolver.query(
      uri,
      projection,
      null, // No selection (get all inbox messages)
      null, // No selection arguments
      "${Telephony.Sms.DATE} DESC" // Sort by date in descending order (newest first)
    )
    
    cursor?.use { // Use `use` block for automatic cursor closing
      if (it.moveToFirst()) {
        val idIndex = it.getColumnIndex(Telephony.Sms._ID)
        val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
        val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
        
        do {
          val id = it.getLong(idIndex)
          val address = it.getString(addressIndex)
          val body = it.getString(bodyIndex)
          val date = it.getLong(dateIndex) // Timestamp in milliseconds
          
          // Basic null checks, especially for address which can sometimes be null
          if (address != null && body != null) {
            smsList.add(SmsMessage(id, address, body, date))
          }
          
        } while (it.moveToNext())
      }
    }
    return smsList
  }
}

// Data class to hold SMS information
data class SmsMessage(
  val id: Long,
  val address: String,
  val body: String,
  val date: Long // Timestamp in milliseconds
)

// Simple Composable to display the SMS list and a button to request permission
@Composable
fun SmsScreen(smsList: List<SmsMessage>,
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
        val inputData: String = sms.body
        val verdict: String = runInference(modelFile, inputData)

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
          Text("From: ${sms.address}")
          Text("Message: ${sms.body}")
          Text(
            "Date: ${
              SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
              ).format(Date(sms.date))
            }"
          )
          // Show the verdict of the message. (Spam or not).
          Text(verdict, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
