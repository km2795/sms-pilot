package com.example.smspilot

/**
 * SMS message unit (single message).
 */
class Message (
  private var id: Long = 0,
  private var address: String = "",
  private var body: String = "",
  private var date: Long = 0) {

  /**
   * Get ID of the message.
   */
  fun getId(): Long{
    return this.id
  }

  /**
   * Set ID of the message.
   */
  private fun setId(id: Long) {
    this.id = id
  }

  /**
   * Get address of the sender.
   */
  fun getAddress(): String {
    return this.address
  }

  /**
   * Set address of the sender.
   */
  private fun setAddress(address: String) {
    this.address = address
  }

  /**
   * Get body of the message.
   */
  fun getBody(): String {
    return this.body
  }

  /**
   * Set body of the message.
   */
  private fun setBody(body: String) {
    this.body = body
  }

  /**
   * Get date of the message.
   */
  fun getDate(): Long {
    return this.date
  }

  /**
   * Set date of the message.
   */
  fun setDate(date: Long) {
    this.date = date
  }
}