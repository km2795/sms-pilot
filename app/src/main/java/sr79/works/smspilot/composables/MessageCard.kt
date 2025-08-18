package sr79.works.smspilot

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MessageCard(
  message: Message,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp, horizontal = 8.dp),

    // Arrangement of the children component.
    horizontalArrangement = if (message.getType() == 1) Arrangement.Start else Arrangement.End
  ) {
    // To limit the size up to 75% of the parent component.
    BoxWithConstraints {
      val cardMaxWidth = maxWidth * 0.75f
      ElevatedCard(
        elevation =
          CardDefaults
            .cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .padding(vertical = 4.dp, horizontal = 8.dp)
          .widthIn(max = cardMaxWidth)
      ) {
        Box(modifier = Modifier.background(Color.White)) {
          Column(
            modifier = Modifier
              .padding(10.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
            message.getBody(),
              fontSize = 14.sp,
              modifier = Modifier.padding(end = 20.dp)
            )
            Text(
            Utilities.modifyDateField(message.getDate().toString(), false),
              fontSize = 10.sp,
              color = Color.Gray
            )

            // Show the verdict of the message. (Spam or not).
          if (message.getSpamOrNot() == true) {
            Text("SPAM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
          }
          }
        }
      }
    }
  }
}