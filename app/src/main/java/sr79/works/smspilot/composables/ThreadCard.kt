package sr79.works.smspilot.composables

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sr79.works.smspilot.R
import sr79.works.smspilot.Thread
import sr79.works.smspilot.ThreadActivity
import sr79.works.smspilot.Utilities
import java.nio.MappedByteBuffer


@Composable
fun ThreadCard(
  sms: Thread,
  modelFile: MappedByteBuffer?,
  modifier: Modifier = Modifier
) {
  val smsBody: String = sms.getBodyThumbnail()
  val context = LocalContext.current

  ElevatedCard(
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

    // When the 'ThreadCard' is clicked.
    onClick = {

      // Create an intent to pass Thread Object to 'ThreadActivity'
      val intent = Intent(context, ThreadActivity::class.java)

      // @param {sms} is to be serialized.
      intent.putExtra("thread", Utilities.serialize(sms))
      intent.putExtra("detector", Utilities.serialize(modelFile))

      // Start the 'ThreadActivity'.
      context.startActivity(intent)
    },
    modifier = Modifier.padding(5.dp)
  ) {
    Box(modifier = Modifier.background(Color.White)) {
      Row(modifier = Modifier.padding(10.dp)) {
        Box(modifier = Modifier) {
          Image(
            painter = painterResource(id = R.drawable.user_foreground),
            contentDescription = "Contact Image",
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(Color.DarkGray),
            modifier = modifier
              .size(50.dp)
              .clip(CircleShape)
              .border(0.5.dp, Color.Black, CircleShape)
          )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              sms.getAddress(),
              modifier.padding(horizontal = 15.dp, vertical = 3.dp)
            )
            Text(
              Utilities.modifyDateField(sms.getShowDate().toString(), false),
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
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.weight(1f)) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(20.dp)
                  .clip(CircleShape)
                  .border(1.dp, Color.Black, CircleShape)
              ) {
                Text(
                  sms.getThreadSize().toString(),
                  fontSize = 13.sp
                )
              }
            }
          }
          if (sms.hasSpamOrNot()) {
            Text(
              "SPAM DETECTED: ${sms.getSpamCount()}",
              fontSize = 13.sp,
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