package sr79.works.smspilot

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.nio.MappedByteBuffer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadPage(
  topBarName: String,
  modelFile: MappedByteBuffer?,
  smsList: List<Message>,
  modifier: Modifier = Modifier
) {

  Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    TopAppBar(
      title = { Text(topBarName) },
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
      modelFile,
      modifier = Modifier
    )
  }
}