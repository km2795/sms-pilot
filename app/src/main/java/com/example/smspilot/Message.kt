package com.example.smspilot

/**
 * SMS message unit (single message).
 */
class Message (
  private var id: Long = 0,
  private var address: String = "",
  private var body: String = "",
  private var date: Long = 0,

  // Type of the message (sent or received).
  // 2 if the message is sent.
  // 1 if the message is received.
  private var type: Int = 1,
  private var spamOrNot: Boolean = false
) {

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

  /**
   * Get status of the message.
   *
   * 2 if the message is sent.
   * 1 if the message is received.
   *
   * @return Boolean
   */
  fun getType(): Int {
    return this.type
  }

  /**
   * Set status of the message.
   *
   * @param status Boolean
   */
  fun setType(status: Int) {
    this.type = status
  }

  /**
   * Get spam or not.
   *
   * @return Boolean
   */
  fun getSpamOrNot(): Boolean {
    return this.spamOrNot
  }

  /**
   * Set spam or not.
   *
   * @param spamOrNot Boolean
   */
  fun setSpamOrNot(spamOrNot: Boolean) {
    this.spamOrNot = spamOrNot
  }
}