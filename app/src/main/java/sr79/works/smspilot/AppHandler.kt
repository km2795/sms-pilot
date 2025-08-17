package sr79.works.smspilot

import android.app.Activity
import android.content.ContentResolver
import android.provider.Telephony
import sr79.works.smspilot.composables.DisplayThread
import java.nio.MappedByteBuffer

/**
 * Handler class for app's main logic.
 */
object AppHandler {

  fun setupDetector(activity: Activity): MappedByteBuffer? {
    return loadDetector(activity, "sms_spam_detector_model.tflite")
  }

  /**
   * Returns the list of all the SMSs in the inbox ('inbox', 'sent').
   *
   * @param detector MappedByteBuffer (Spam detector).
   * @param contentResolver Content resolver.
   * @return List of messages.
   */
  fun getMessageList(
    detector: MappedByteBuffer?,
    contentResolver: ContentResolver
  ): List<Message> {

    val messageList = mutableListOf<Message>()

    // List of URI to fetch from.
    val uriList = listOf(
      Telephony.Sms.Inbox.CONTENT_URI,  // Inbox.
      Telephony.Sms.Sent.CONTENT_URI    // Sent.
    )

    // Columns to retrieve
    val projection = arrayOf(
      Telephony.Sms._ID,
      Telephony.Sms.ADDRESS,
      Telephony.Sms.BODY,
      Telephony.Sms.DATE,
      Telephony.Sms.TYPE
    )

    uriList.forEach { uri ->
      contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${Telephony.Sms.DATE} DESC"
      )?.use { it ->
        if (it.moveToFirst()) {
          val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
          val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
          val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
          val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
          val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)

          do {
            val id = it.getLong(idIndex)
            val address = it.getString(addressIndex)
            val body = it.getString(bodyIndex)
            val date = it.getLong(dateIndex) // Timestamp in milliseconds
            val type = it.getInt(typeIndex)

            // Basic null checks, especially for address which can sometimes be null
            if (address != null && body != null) {
              // Add the message.
              messageList.add(
                Message(id, address, body, date, type)
              )
            }

          } while (it.moveToNext())
        }
      }
    }

    return messageList
  }

  /**
   * Returns the list of all the Threads.
   * Each message is put into its respective Thread.
   *
   * @param detector MappedByteBuffer (Spam detector).
   * @param contentResolver Content resolver.
   * @return List of messages.
   */
  fun getThreadList(
    detector: MappedByteBuffer?,
    contentResolver: ContentResolver
  ): List<Thread> {

    val threadMap: MutableMap<String, Thread> = mutableMapOf()
    val messages = getMessageList(detector, contentResolver)

    messages.forEach { message ->
      ThreadListHandler.addMessage(threadMap, message)
    }

    return ThreadListHandler.getThreadList(threadMap)
  }

  fun getDisplayThreads(
    threadList: List<Thread>
  ): List<DisplayThread> {

    val displayThreads: MutableList<DisplayThread> = mutableListOf()

    threadList.forEach { thread->
      displayThreads.add(DisplayThread(
        id = thread.getThreadId().toString(),
        address = thread.getAddress(),
        contactPlaceholder = Utilities.placeholderForContact(thread.getAddress()),
        showDate = Utilities.modifyDateField(thread.getShowDate().toString(), false),
        bodyThumbnail = thread.getBodyThumbnail(),
        threadSize = thread.getThreadSize(),
        isSpam = thread.hasSpamOrNot(),
        spamCount = thread.getSpamCount(),
        ogThread = thread
        )
      )
    }

    return displayThreads
  }
}
