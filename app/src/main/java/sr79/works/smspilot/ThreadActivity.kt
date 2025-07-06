package sr79.works.smspilot

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier

class ThreadActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Deserialize the Thread object.
    val sms: Thread? = intent.getStringExtra("thread")?.let { value ->
      Utilities.deserialize<Thread>(value)
    }

    setContent {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        if (sms != null) {
          // Top Bar Title changes to Address of the Message.
          val topBarName = sms.getAddress()

          // Load the message list.
          ThreadPage(
            topBarName,
            APP.detector,
            sms.getMessageListAscending(),
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
