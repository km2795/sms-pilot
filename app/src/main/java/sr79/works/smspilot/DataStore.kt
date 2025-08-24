package sr79.works.smspilot

import android.R.attr.type
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Data storage interface of the application.
 */
class DataStore(context: Context): SQLiteOpenHelper(
  context, DATABASE_NAME,
  null,
  DATABASE_VERSION
) {

  companion object {
    const val DATABASE_NAME = "sms_ds_main.db"
    const val DATABASE_VERSION = 1
    const val TABLE_NAME = "sms_data_store"
    const val COLUMN_NAME_ID = "id"
    const val COLUMN_NAME_ADDRESS = "address"
    const val COLUMN_NAME_BODY = "body"
    const val COLUMN_NAME_DATE = "date"
    const val COLUMN_NAME_TYPE = "type"
    const val COLUMN_NAME_SPAM = "spam"
  }

  // SMS read permission flag.
  private var SMS_READ_PERMISSION: Boolean = false

  // Create table query.
  private val CREATE_TABLE = "" +
          "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
          COLUMN_NAME_ID + " LONG PRIMARY KEY," +
          COLUMN_NAME_ADDRESS + " TEXT," +
          COLUMN_NAME_BODY + " TEXT," +
          COLUMN_NAME_DATE + " INTEGER," +
          COLUMN_NAME_TYPE + " INTEGER," +
          COLUMN_NAME_SPAM + " TEXT" +
          ")"

  /**
   * To clear the table.
   */
  fun clearTable() {
    val db = this.writableDatabase
    db.execSQL("DELETE FROM $TABLE_NAME")
  }

  /**
   * To delete the table.
   */
  fun deleteTable() {
    val db = this.writableDatabase
    db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
  }

  /**
   * To store a message.
   *
   * @param id ID of the message.
   * @param body Body of the message.
   * @param date Date of the message.
   * @param type Type of the message.
   * @param spam Spam status of the message.
   */
  fun storeMessage(message: Message): Boolean {
    val entryMap = ContentValues().apply {
      put(COLUMN_NAME_ID, message.getId())
      put(COLUMN_NAME_BODY, message.getBody())
      put(COLUMN_NAME_ADDRESS, message.getAddress())
      put(COLUMN_NAME_DATE, message.getDate())
      put(COLUMN_NAME_TYPE, message.getType())
      put(COLUMN_NAME_SPAM, message.getSpamOrNot())
    }
    val db = this.writableDatabase
    return try {
      val rowId = db.insert(TABLE_NAME, null, entryMap)

      // Check to see, if the message got inserted.
      rowId != -1L
    } catch (e: Exception) {
      Log.e("DataStore", "Error while inserting message", e)
      false
    }
  }

  /**
   * To update an existing message in the database.
   * The message is identified by its ID.
   *
   * @param message The message object containing the updated data.
   * @return True if the message was updated successfully (at least one row affected), false otherwise.
   */
  fun updateMessage(message: Message): Boolean {
    val db = this.writableDatabase
    val values = ContentValues().apply {
      put(COLUMN_NAME_ADDRESS, message.getAddress())
      put(COLUMN_NAME_BODY, message.getBody())
      put(COLUMN_NAME_DATE, message.getDate())
      put(COLUMN_NAME_TYPE, message.getType())
      put(COLUMN_NAME_SPAM, message.getSpamOrNot())
    }

    // Define the WHERE clause: update the row where its ID matches the message's ID.
    val selection = "$COLUMN_NAME_ID = ?"
    val selectionArgs = arrayOf(message.getId().toString()) // The ID of the message to update

    return try {
      val count = db?.update(
        TABLE_NAME,
        values,
        selection,
        selectionArgs
      )
      // The update() method returns the number of rows affected.
      // If count is greater than 0, it means the update was successful for at least one row.
      count != null && count > 0
    } catch (e: Exception) {
      Log.e("DataStore", "Error while updating message with id ${message.getId()}", e)
      false
    }
  }

  /**
   * To store messages in bulk or a complete message list.
   *
   * @param messageList List of messages.
   */
  fun storeMessageList(messageList: List<Message>) {
    val db = this.writableDatabase

    for (message in messageList) {
      val entryMap = ContentValues().apply {
        put(COLUMN_NAME_ID, message.getId())
        put(COLUMN_NAME_BODY, message.getBody())
        put(COLUMN_NAME_ADDRESS, message.getAddress())
        put(COLUMN_NAME_DATE, message.getDate())
        put(COLUMN_NAME_TYPE, message.getType())
        put(COLUMN_NAME_SPAM, message.getSpamOrNot())
      }
      db?.insert(TABLE_NAME, null, entryMap)
    }
  }

  /**
   * To get the number of rows in the table.
   *
   * @return Number of rows.
   */
  private fun getRowCount(): Int {
    val db = this.readableDatabase
    val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    val count = cursor.count
    cursor.close()
    return count
  }

  /**
   * To load the message list.
   *
   * @return List of messages.
   */
  fun loadMessageList(): List<Message>? {
    return if (getRowCount() > 0) {
      getMessageList()
    } else {
      null
    }
  }

  /**
   * To get the message list.
   *
   * @return List of messages.
   */
  private fun getMessageList(): List<Message> {
    val db = this.readableDatabase

    val projection = arrayOf(
      COLUMN_NAME_ID,
      COLUMN_NAME_BODY,
      COLUMN_NAME_ADDRESS,
      COLUMN_NAME_DATE,
      COLUMN_NAME_TYPE,
      COLUMN_NAME_SPAM
    )

    val sortOrder = "$COLUMN_NAME_DATE DESC"

    val cursor = db.query(
      TABLE_NAME,
      projection,
      null,
      null,
      null,
      null,
      sortOrder
    )

    val messageList = mutableListOf<Message>()
    with(cursor) {
      while (moveToNext()) {
        val id = getLong(getColumnIndexOrThrow(COLUMN_NAME_ID))
        val body = getString(getColumnIndexOrThrow(COLUMN_NAME_BODY))
        val address = getString(getColumnIndexOrThrow(COLUMN_NAME_ADDRESS))
        val date = getLong(getColumnIndexOrThrow(COLUMN_NAME_DATE))
        val type = getInt(getColumnIndexOrThrow(COLUMN_NAME_TYPE))
        val spam: Boolean? =
          Utilities.stringToBoolean(getInt(getColumnIndexOrThrow(COLUMN_NAME_SPAM)).toString())
        messageList.add(
          Message(
            id = id,
            body = body,
            address = address,
            date = date,
            type = type,
            spamOrNot = spam
          )
        )
      }
    }
    cursor.close()

    return messageList
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(CREATE_TABLE)
  }

  override fun onUpgrade(
    db: SQLiteDatabase,
    oldVersion: Int,
    newVersion: Int
  ) {

    db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    onCreate(db)
  }

  /**
   * Update the SMS read permission.
   *
   * @param permission SMS read permission.
   */
  fun updateSmsReadPermission(
    context: Context,
    permission: Boolean
  ) {

    SMS_READ_PERMISSION = permission
    Utilities.writeFile(
      context,
      "sms_read_permission",
      permission.toString()
    )
  }

  /**
   * Return the SMS read permission.
   *
   * @return SMS read permission.
   */
  fun getSmsReadPermission(context: Context): Boolean {
    SMS_READ_PERMISSION = Utilities.readFile(
      context,
      "sms_read_permission"
    )?.toBoolean() ?: false

    return SMS_READ_PERMISSION
  }
}