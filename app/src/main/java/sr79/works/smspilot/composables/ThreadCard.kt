package sr79.works.smspilot.composables

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sr79.works.smspilot.ThreadActivity
import sr79.works.smspilot.Utilities


@Composable
fun ThreadCard(
  thread: DisplayThread,
  modifier: Modifier = Modifier
) {
  val smsBody: String = thread.bodyThumbnail
  val context = LocalContext.current

  Card(
    // When the 'ThreadCard' is clicked.
    onClick = remember(context, thread) {{

      // Create an intent to pass Thread Object to 'ThreadActivity'
      val intent = Intent(context, ThreadActivity::class.java)

      // @param {Thread} is to be serialized.
      intent.putExtra("thread", Utilities.serialize(thread.ogThread))

      // Start the 'ThreadActivity'.
      context.startActivity(intent)
      }
    },
    modifier = Modifier.padding(8.dp)
  ) {
    Box(modifier = Modifier.background(Color.White)) {
      Row(modifier = Modifier.padding(10.dp)) {
        Box(
          modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .border(0.5.dp, Color.Black, CircleShape),
          contentAlignment = Alignment.Center // Center the text
        ) {
          Text(
            text = thread.contactPlaceholder,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
          )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              thread.address,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 3.dp)
            )
            Text(
              thread.showDate,
              textAlign = TextAlign.Right,
              fontSize = 13.sp,
              modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 3.dp)
                .weight(1f)
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              smsBody,
              modifier
                .padding(horizontal = 15.dp, vertical = 3.dp)
                .weight(8f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              fontStyle = FontStyle.Italic,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.weight(1f)) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .border(0.5.dp, Color.Black, CircleShape)
              ) {
                Text(
                  thread.threadSize.toString(),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
          if (thread.isSpam) {
            Text(
              "SPAM DETECTED: ${thread.spamCount}",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.Red,
              modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 3.dp)
            )
          }
        }
      }
    }
  }
}
