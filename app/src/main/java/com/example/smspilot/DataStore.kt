package com.example.smspilot

import android.content.Context

/**
 * Data storage interface of the application.
 */
class DataStore {
  private var SMS_READ_PERMISSION: Boolean = false

  /**
   * Update the SMS read permission.
   *
   * @param permission SMS read permission.
   */
  fun updateSmsReadPermission(context: Context, permission: Boolean) {
    this.SMS_READ_PERMISSION = permission
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
    this.SMS_READ_PERMISSION = Utilities.readFile(
      context,
      "sms_read_permission")?.toBoolean() ?: false

    return this.SMS_READ_PERMISSION
  }
}