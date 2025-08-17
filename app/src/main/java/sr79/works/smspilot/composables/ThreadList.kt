package sr79.works.smspilot.composables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


// Simple Composable to display the SMS list and a button to request permission
@Composable
fun ThreadList(
  displayThreads: List<DisplayThread>,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()

      /*
       * To let threads slide over the navigation,
       * otherwise, they'd be covered by the navigation.
       */
      .padding(WindowInsets.navigationBars.asPaddingValues())
  ) {
    items(displayThreads) {thread ->
      ThreadCard(thread, modifier)
    }
  }
}
