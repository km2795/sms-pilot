package sr79.works.smspilot.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import sr79.works.smspilot.Thread
import java.nio.MappedByteBuffer

// Simple Composable to display the SMS list and a button to request permission
@Composable
fun ThreadList(
  threadList: List<Thread>,
  detector: MappedByteBuffer?,
  modifier: Modifier = Modifier
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
    threadList.forEach { message ->
      ThreadCard(message, detector, modifier)
    }
  }
}
