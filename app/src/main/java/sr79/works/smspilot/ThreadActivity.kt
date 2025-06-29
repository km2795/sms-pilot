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

    val threadId = intent.getStringExtra("THREAD_ID")
    val sms = SmsList.getThread(APP.SMS_LIST_MAP, threadId!!)

    setContent {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        if (sms != null) {
          // Top Bar Title changes to Address of the Message.
          var topBarName = sms.getAddress()

          // Load the message list.
          CurrentPage(
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
