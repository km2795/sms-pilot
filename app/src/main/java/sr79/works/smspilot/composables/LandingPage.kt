package sr79.works.smspilot.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sr79.works.smspilot.APP_TITLE
import sr79.works.smspilot.Thread
import java.nio.MappedByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingPage(
  smsList: List<Thread>,
  modelFile: MappedByteBuffer?,
  showPermissionButton: Boolean,
  onRequestPermission: () -> Unit,
  onShowPermissionButton: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {

  // For controlling visibility of the extra top bar actions.
  var showExtraTopActionMenu by rememberSaveable { mutableStateOf(false) }

  Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    TopAppBar(
      title = { Text(APP_TITLE) },
      navigationIcon = {},
      actions = {
        TopBarActions(
          showMenu = showExtraTopActionMenu,
          onShowExtraTopActionMenu = { showExtraTopActionMenu = it },
          onShowPermissionButton,
          modifier = modifier
        )
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.hsv(270f, 0.4f, 0.3f),
        titleContentColor = Color.hsv(0f, 0f, 1f)
      ),
      modifier = Modifier.shadow(6.dp)
    )
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
        ThreadList(
          smsList,
          modifier = Modifier
        )
      }
    }
  }
}
