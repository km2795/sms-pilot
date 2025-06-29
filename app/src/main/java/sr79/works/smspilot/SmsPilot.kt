package sr79.works.smspilot

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import java.nio.MappedByteBuffer

/**
 * Main Class where the app's logic reside or
 * is initiated from.
 */
class SmsPilot {

  /**
   * Get the SMS read permission from the data cache.
   *
   * @param context Context of the application.
   * @return SMS read permission.
   */
  fun checkSmsReadPermission(context: Context): Boolean {
    return DataStore().getSmsReadPermission(context)
  }

  /**
   * Update the SMS read permission in the data cache.
   *
   * @param permission SMS read permission.
   * @param context Context of the application.
   */
  fun updateSmsReadPermission(context: Context, permission: Boolean) {
    DataStore().updateSmsReadPermission(context, permission)
  }

  fun setupDetector(activity: Activity): MappedByteBuffer? {
    return loadModelFile(activity, "sms_spam_detector_model.tflite")
  }

  /**
   * Load the SMS list.
   *
   * @param contentResolver Content resolver.
   * @return SMS list.
   */
  fun getSmsList(contentResolver: ContentResolver) : MutableList<Message> {
    return fetchSms(contentResolver)
  }

  /**
   * Unload the SMS list.
   */
  fun unLoadSmsList(map: MutableMap<String, Thread>, list: MutableList<Thread>) {
    SmsList.clearList(map, list)
  }

  /**
   * Wrapper around fetchSmsMessages.
   *
   * @param contentResolver Content resolver.
   * @return SMS list.
   */
  private fun fetchSms(contentResolver: ContentResolver): MutableList<Message> {
    return fetchSmsMessages(contentResolver).toMutableList()
  }

  /**
   * Returns the list of all the SMSs in the inbox ('inbox').
   *
   * @param contentResolver Content resolver.
   * @return List of messages.
   */
  private fun fetchSmsMessages(contentResolver: ContentResolver): List<Message> {
    val smsList: MutableList<Message> = mutableListOf<Message>()

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

    // You can add a selection and selectionArgs to filter messages
    // For works, to get messages from a specific number:
    // val selection = "${Telephony.Sms.ADDRESS} = ?"
    // val selectionArgs = arrayOf("1234567890")
    // Or to get messages after a certain date:
    // val selection = "${Telephony.Sms.DATE} > ?"
    // val selectionArgs = arrayOf(specificTimestamp.toString())

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
              smsList.add(Message(id, address, body, date, type, spamOrNot(APP.detector, body)))
            }

          } while (it.moveToNext())
        }
      }
    }

    return smsList
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

    // To clear the list, to avoid redundancy.
    // This is very inefficient.
    SmsList.clearList(smsMap, APP.SMS_LIST)

    for (message in messageList) {
      SmsList.addMessage(smsMap, message)
    }
    return SmsList.getThreadList(smsMap)
  }
}
