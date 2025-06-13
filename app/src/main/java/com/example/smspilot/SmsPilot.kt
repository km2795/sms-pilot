package com.example.smspilot

import android.app.Activity
import android.content.ContentResolver
import android.provider.Telephony
import android.database.Cursor
import java.nio.MappedByteBuffer

/**
 * Main Class where the app's logic reside or
 * is initiated from.
 */
class SmsPilot {
  private var smsList = SmsList()

  fun setupDetector(activity: Activity): MappedByteBuffer? {
    return loadModelFile(activity, "sms_spam_detector_model.tflite")
  }

  fun fetchSmsMessages(contentResolver: ContentResolver): List<Thread> {
    val uri = Telephony.Sms.Inbox.CONTENT_URI

    // Columns to retrieve
    val projection = arrayOf(
      Telephony.Sms._ID,
      Telephony.Sms.ADDRESS,
      Telephony.Sms.BODY,
      Telephony.Sms.DATE
    )

    // You can add a selection and selectionArgs to filter messages
    // For example, to get messages from a specific number:
    // val selection = "${Telephony.Sms.ADDRESS} = ?"
    // val selectionArgs = arrayOf("1234567890")
    // Or to get messages after a certain date:
    // val selection = "${Telephony.Sms.DATE} > ?"
    // val selectionArgs = arrayOf(specificTimestamp.toString())

    val cursor: Cursor? = contentResolver.query(
      uri,
      projection,
      null, // No selection (get all inbox messages)
      null, // No selection arguments
      "${Telephony.Sms.DATE} DESC" // Sort by date in descending order (newest first)
    )

    cursor?.use {
      if (it.moveToFirst()) {
        val idIndex = it.getColumnIndex(Telephony.Sms._ID)
        val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
        val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
        val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

        do {
          val id = it.getLong(idIndex)
          val address = it.getString(addressIndex)
          val body = it.getString(bodyIndex)
          val date = it.getLong(dateIndex) // Timestamp in milliseconds

          // Basic null checks, especially for address which can sometimes be null
          if (address != null && body != null) {
            // Add the message.
            smsList.addMessage(Message(id, address, body, date))
          }

        } while (it.moveToNext())
      }
    }
    return smsList.getThreadList()
  }
}