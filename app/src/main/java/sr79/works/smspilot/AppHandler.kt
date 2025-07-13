package sr79.works.smspilot

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import java.nio.MappedByteBuffer

/**
 * Handler class for app's main logic.
 */
object AppHandler {

  /**
   * Get the SMS read permission from the data cache.
   *
   * @param context Context of the application.
   * @return SMS read permission.
   */
  fun checkSmsReadPermission(dataStore: DataStore, context: Context): Boolean {
    return dataStore.getSmsReadPermission(context) ?: false
  }

  /**
   * Update the SMS read permission in the data cache.
   *
   * @param permission SMS read permission.
   * @param context Context of the application.
   */
  fun updateSmsReadPermission(dataStore: DataStore, context: Context, permission: Boolean) {
    dataStore.updateSmsReadPermission(context, permission)
  }

  fun setupDetector(activity: Activity): MappedByteBuffer? {
    return loadModelFile(activity, "sms_spam_detector_model.tflite")
  }

  /**
   * Returns the list of all the SMSs in the inbox ('inbox', 'sent').
   *
   * @param detector MappedByteBuffer (Spam detector).
   * @param contentResolver Content resolver.
   * @return List of messages.
   */
  fun getMessageList(detector: MappedByteBuffer?, contentResolver: ContentResolver): List<Message> {
    val messageList: MutableList<Message> = mutableListOf<Message>()

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
      val cursor: Cursor? = contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${Telephony.Sms.DATE} DESC"
      )

      cursor?.use {
        if (it.moveToFirst()) {
          val idIndex = it.getColumnIndex(Telephony.Sms._ID)
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
              messageList.add(Message(id, address, body, date, type, spamOrNot(detector, body)))
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
  fun getThreadList(detector: MappedByteBuffer?, contentResolver: ContentResolver): List<Thread> {
    val threadMap: MutableMap<String, Thread> = mutableMapOf()

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
      val cursor: Cursor? = contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${Telephony.Sms.DATE} DESC"
      )

      cursor?.use {
        if (it.moveToFirst()) {
          val idIndex = it.getColumnIndex(Telephony.Sms._ID)
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
              val message = Message(id, address, body, date, type, spamOrNot(detector, body))
              SmsListHandler.addMessage(threadMap, message)
            }

          } while (it.moveToNext())
        }
      }
    }

    return SmsListHandler.getThreadList(threadMap)
  }

  /**
   * Convert the list of messages into a List of Threads.
   *
   * @param messageList List of messages.
   * @return List of threads.
   */
  fun formAndGetThreadList(
    smsMap: MutableMap<String, Thread>,
    messageList: MutableList<Message>
  ): List<Thread> {

    for (message in messageList) {
      SmsListHandler.addMessage(smsMap, message)
    }
    return SmsListHandler.getThreadList(smsMap)
  }
}
