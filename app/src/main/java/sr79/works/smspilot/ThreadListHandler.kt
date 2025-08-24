package sr79.works.smspilot

/**
 * Handler class for maps and lists.
 */
object ThreadListHandler {

  /**
   * Add message to a Thread in the list.
   *
   * @param message Message to add.
   */
  fun addMessage(
    smsMap: MutableMap<String, Thread>,
    message: Message
  ) {

    val address = message.getAddress()
    if (smsMap[address] != null) {
      smsMap[address]?.addMessage(message)
    } else {
      smsMap[address] = Thread(message, address)
    }
  }

  /**
   * Return the complete Thread list.
   */
  fun getThreadList(
    smsMap: MutableMap<String, Thread>
  ): MutableList<Thread> {

    return smsMap
      .values
      .toList()
      .sortedByDescending { it.getShowDate() }
      .toMutableList()
  }
}