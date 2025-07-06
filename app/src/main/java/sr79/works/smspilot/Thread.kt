package sr79.works.smspilot

import kotlinx.serialization.Serializable


/**
 * List of Messages with same address.
 */
@Serializable
class Thread(
  private var messageList: MutableSet<Message>? = mutableSetOf<Message>(),
  private var id: Long = 0,
  private var address: String = "",
  private var bodyThumbnail: String = "",
  private var showDate: Long = 0,
  private var hasSpam: Int = 0
) {

  constructor(message: Message, address: String) : this() {
    this.messageList?.add(message)
    this.address = address
    this.id = message.getId()
    this.updateThumbnailData(message)
  }

  fun getThreadId(): Long {
    return this.id
  }

  /**
   * Returns the messages in date sorted (descending) format.
   *
   * @return List of messages in date sorted (descending) format.
   */
  fun getMessageListDescending(): MutableList<Message> {
    return this.messageList?.sortedByDescending { it.getDate() }?.toMutableList() ?: mutableListOf()
  }

  /**
   * Returns the messages in date sorted (ascending) format.
   *
   * @return List of messages in date sorted (ascending) format.
   */
  fun getMessageListAscending(): MutableList<Message> {
    return this.messageList?.sortedBy { it.getDate() }?.toMutableList() ?: mutableListOf()
  }

  fun getThreadSize(): Int {
    return this.messageList?.size ?: 0
  }

  /**
   * Create message list from a list of messages.
   *
   * @param message Message to add.
   */
  fun addMessage(message: Message) {
    this.messageList?.add(message)

    /*
     * This will be helpful when an empty Thread is
     * created and a new Message was pushed in this
     * thread. The address parameter would not have
     * updated.
     */
    this.address = message.getAddress()
    this.id = message.getId()
    this.updateThumbnailData(message)
    val verdict = message.getSpamOrNot()
    if (verdict) {
      this.hasSpam++
    }
  }

  /**
   * Address of the thread.
   *
   * @return Address of the thread.
   */
  fun getAddress(): String {
    return this.address
  }

  /**
   * Set address of the thread.
   *
   * @param address Address of the thread.
   */
  private fun setAddress(address: String) {
    this.address = address
  }

  /**
   * Return the message to show as thumbnail or peak view.
   *
   * @return Message to show as thumbnail.
   */
  fun getBodyThumbnail(): String {
    return this.bodyThumbnail
  }

  /**
   * Updates the message thumbnail and date to be
   * shown in the peak view.
   *
   * @param message Message as reference.
   */
  private fun updateThumbnailData(message: Message) {
    if (message.getDate() > this.showDate) {
      this.updateShowDate(message.getDate())
      this.updateBodyThumbnail(message.getBody())
    }
  }

  /**
   * Updates the message to show as thumbnail or peak view
   * to the contents of the thread (the latest message).
   *
   * @param message Message to show as thumbnail.
   */
  private fun updateBodyThumbnail(message: String) {
    this.bodyThumbnail = message
  }

  /**
   * Return the date to show in the thread's view.
   * (latest message date).
   *
   * @return Date to show in the thread's view.
   */
  fun getShowDate(): Long {
    return this.showDate
  }

  /**
   * Updates the latest show date (most recent message received
   * or entered in the phone)
   *
   * @param date Date of the latest message.
   */
  private fun updateShowDate(date: Long) {
   this.showDate = date
  }

  fun hasSpamOrNot(): Boolean {
    return this.hasSpam > 0
  }

}