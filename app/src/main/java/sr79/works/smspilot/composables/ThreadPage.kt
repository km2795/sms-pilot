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
import sr79.works.smspilot.ui.theme.OrangeDef

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadPage(
  topBarName: String,
  messageList: List<Message>,
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
        containerColor = OrangeDef,
        titleContentColor = Color.White
      ),
      modifier = Modifier.shadow(6.dp)
    )
    MessageList(
      messageList,
      modifier = Modifier
    )
  }
}