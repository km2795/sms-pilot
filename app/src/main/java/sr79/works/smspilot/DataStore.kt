package sr79.works.smspilot

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
          COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
          COLUMN_NAME_ADDRESS + " TEXT," +
          COLUMN_NAME_BODY + " TEXT," +
          COLUMN_NAME_DATE + " INTEGER," +
          COLUMN_NAME_TYPE + " INTEGER," +
          COLUMN_NAME_SPAM + " INTEGER" +
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
  fun storeMessage(
    id: Long,
    body: String,
    address: String,
    date: Long,
    type: Int,
    spam: Int
  ) {

    val entryMap = ContentValues().apply {
      put(COLUMN_NAME_ID, id)
      put(COLUMN_NAME_BODY, body)
      put(COLUMN_NAME_ADDRESS, address)
      put(COLUMN_NAME_DATE, date)
      put(COLUMN_NAME_TYPE, type)
      put(COLUMN_NAME_SPAM, spam)
    }
    val db = this.writableDatabase
    db?.insert(TABLE_NAME, null, entryMap)
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
        val id = getInt(getColumnIndexOrThrow(COLUMN_NAME_ID))
        val body = getString(getColumnIndexOrThrow(COLUMN_NAME_BODY))
        val address = getString(getColumnIndexOrThrow(COLUMN_NAME_ADDRESS))
        val date = getLong(getColumnIndexOrThrow(COLUMN_NAME_DATE))
        val type = getInt(getColumnIndexOrThrow(COLUMN_NAME_TYPE))
        val spam = getInt(getColumnIndexOrThrow(COLUMN_NAME_SPAM))
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