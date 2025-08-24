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

  /**
   * Sets up the spam detector model.
   *
   * @param activity The current activity.
   * @return MappedByteBuffer containing the loaded model, or null if loading failed.
   */
  fun setupDetector(activity: Activity): MappedByteBuffer? {
    return loadDetector(activity, "sms_spam_detector_model.tflite")
  }

  /**
   * Retrieves the total number of messages in the inbox and sent folders.
   *
   * This function performs a lightweight query to the content provider to get the count
   * of messages.
   *
   * @param contentResolver The [ContentResolver] to use for querying the SMS content provider.
   * @return The total number of messages in the inbox and sent folders.
   */
  fun getMessageListSize(contentResolver: ContentResolver): Int {
    var messageListSize = 0

    // URIs to query.
    var uriList = listOf(
      Telephony.Sms.Inbox.CONTENT_URI,
      Telephony.Sms.Sent.CONTENT_URI
    )

    // Query.
    uriList.forEach() { uri ->
      contentResolver.query(
        uri,
        arrayOf("COUNT(*) AS count"),
        null,
        null,
        null
      )?.use { cursor ->
        if (cursor.moveToFirst()) {
          val countIndex = cursor.getColumnIndex("count")
          if (countIndex != -1) {
            messageListSize += cursor.getInt(countIndex)
          }
        }
      }
    }

    return messageListSize
  }

  /**
   * Retrieves a list of all SMS messages from the device's inbox and sent folders.
   *
   * This function queries the Android `Telephony.Sms` content provider to fetch
   * messages from both the inbox (`Telephony.Sms.Inbox.CONTENT_URI`) and sent
   * (`Telephony.Sms.Sent.CONTENT_URI`) folders.
   *
   * For each message, it extracts the following details:
   * - `_ID`: The unique ID of the message.
   * - `ADDRESS`: The phone number of the sender/recipient.
   * - `BODY`: The content of the message.
   * - `DATE`: The timestamp of the message (in milliseconds).
   * - `TYPE`: The type of the message (e.g., inbox, sent).
   *
   * Messages where the `ADDRESS` or `BODY` is null are excluded from the result.
   * The returned list of messages is sorted by date in descending order (newest first).
   *
   * @param contentResolver The `ContentResolver` instance used to query the content provider.
   * @return A `List` of `Message` objects, each representing an SMS message.
   *         Returns an empty list if no messages are found or if an error occurs.
   */
  fun getMessageList(
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
              var message = Message(
                id = id,
                address = address,
                body = body,
                date = date,
                type = type,
                spamOrNot = null
              )

              // Add the message.
              messageList.add(message)
            }
          } while (it.moveToNext())
        }
      }
    }

    return messageList
  }

  /**
   * Returns a list of all threads, where
   * each message is grouped into its respective thread.
   *
   * This function takes a list of messages and organizes
   * them into threads based on their conversation context.
   * It utilizes a `ThreadListHandler` to manage the grouping
   * logic.
   *
   * @param messageList The list of messages to be organized into threads.
   * @return A list of `Thread` objects, each representing a distinct conversation thread.
   */
  fun getThreadList(
    messageList: List<Message>
  ): List<Thread> {

    val threadMap: MutableMap<String, Thread> = mutableMapOf()

    messageList.forEach { message ->
      ThreadListHandler.addMessage(threadMap, message)
    }

    return ThreadListHandler.getThreadList(threadMap)
  }

  /**
   * Converts a list of [Thread] objects to a list of [DisplayThread] objects.
   * [DisplayThread] objects are used for UI representation and contain additional
   * information like placeholders and formatted dates.
   *
   * @param threadList The list of [Thread] objects to be converted.
   * @return A list of [DisplayThread] objects ready for display.
   */
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
