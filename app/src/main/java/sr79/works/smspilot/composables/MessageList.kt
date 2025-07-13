package sr79.works.smspilot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// Simple Composable to display the SMS list and a button to request permission
@Composable
fun MessageList(
  messageList: List<Message>,
  modifier: Modifier = Modifier
) {

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
      messageList.forEach { message ->
        MessageCard(message, modifier)
      }
    }
  }
}