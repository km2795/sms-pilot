package sr79.works.smspilot.composables

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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sr79.works.smspilot.Thread
import java.nio.MappedByteBuffer

// Simple Composable to display the SMS list and a button to request permission
@Composable
fun ThreadList(smsList: List<Thread>,
               modelFile: MappedByteBuffer?,
               showPermissionButton: Boolean,
               onRequestPermission: () -> Unit,
               onShowPermissionButton: (Boolean) -> Unit,
               modifier: Modifier = Modifier
) {

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (showPermissionButton) {
      Button(onClick = onRequestPermission) {
        Text("Load SMS Messages")
      }
      if (smsList.isEmpty()) {
        Text(
          "No SMS messages found or permission not granted.",
          modifier = Modifier.padding(16.dp)
        )
      }
    } else {
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
          ThreadCard(sms, modifier)
        }
      }

    }
  }
}