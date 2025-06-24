package sr79.works.smspilot

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ThreadActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val threadId = intent.getStringExtra("THREAD_ID")
    val sms = SmsList().getThread(threadId!!)

    setContent {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        if (sms != null) {

          // Load the message list.
          MainScreen(
            sms.getMessageListAscending(),
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
  smsList: List<Message>,
  modifier: Modifier = Modifier) {

  Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    TopAppBar(
      title = { Text(APP_TITLE) },
      navigationIcon = {},
      actions = {
        IconButton(onClick = {}) {
          Icon(
            imageVector = Icons.Filled.MoreVert,
            tint = Color.White,
            contentDescription = "Navigation Menu",
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.hsv(270f, 0.4f, 0.3f),
        titleContentColor = Color.hsv(0f, 0f, 1f)
      ),
      modifier = Modifier.shadow(6.dp)
    )
    MessageList(
      smsList,
      modifier = Modifier
    )
  }
}

// Simple Composable to display the SMS list and a button to request permission
@Composable
fun MessageList(smsList: List<Message>,
                modifier: Modifier = Modifier) {

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()

        /*
         * To let threads slide over the navigation,
         * otherwise, they'd be covered by the navigation.
         */
        .padding(WindowInsets.navigationBars.asPaddingValues())
        .verticalScroll(rememberScrollState())
    ) {
      // Load each Thread.
      smsList.forEach { sms ->
        MessageCard(sms, modifier)
      }
    }
  }
}

@Composable
fun MessageCard(sms: Message, modifier: Modifier = Modifier) {
  Row(modifier = Modifier
    .fillMaxWidth()
    .padding(vertical = 4.dp, horizontal = 8.dp),

    // Arrangement of the children component.
    horizontalArrangement = if (sms.getType() == 1) Arrangement.Start else Arrangement.End
  ) {
    // To limit the size up to 75% of the parent component.
    BoxWithConstraints {
      val cardMaxWidth = maxWidth * 0.75f
      ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
          .padding(vertical = 4.dp, horizontal = 8.dp)
          .widthIn(max = cardMaxWidth)
      ) {

        Column(modifier = Modifier
          .padding(10.dp),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            sms.getBody(),
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 20.dp)
          )
          Text(
            Utilities.modifyDateField(sms.getDate().toString(), false),
            fontSize = 10.sp,
            color = Color.Gray
          )
        }
      }
    }
  }
}
