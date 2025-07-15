package sr79.works.smspilot.composables

import androidx.compose.runtime.Immutable
import sr79.works.smspilot.Thread

@Immutable
data class DisplayThread(
  val id: String,
  val address: String,
  val contactPlaceholder: String,
  val showDate: String,
  val bodyThumbnail: String,
  val threadSize: Int,
  val isSpam: Boolean,
  val spamCount: Int,
  val ogThread: Thread
)
