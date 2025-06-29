package sr79.works.smspilot

/**
 * Handler class for global SMS LIST.
 */
object SmsListHandler {
  /**
   * Add message to a thread in the list.
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
   * Return a thread from the list.
   *
   * @param address Address of the thread.
   */
  fun getThread(
    smsMap: MutableMap<String, Thread>,
    address: String
  ): Thread? {

    return smsMap[address]
  }

  /**
   * Return the complete list.
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

  /**
   * Clear the lists.
   */
  fun clearList(
    smsMap: MutableMap<String, Thread>,
    smsList: MutableList<Thread>
  ) {
    // Clear the thread list.
    smsList.clear()

    // Clear the thread map.
    smsMap.clear()
  }
}