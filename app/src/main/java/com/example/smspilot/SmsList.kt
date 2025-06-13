package com.example.smspilot


/**
 * Main SMS list.
 */
class SmsList {
  private var smsList: MutableMap<String, Thread> = HashMap<String, Thread>()

  /**
   * Add message to a thread in the list.
   *
   * @param message Message to add.
   */
  fun addMessage(message: Message) {
    val address = message.getAddress()
    if (smsList[address] != null) {
      smsList[address]?.addMessage(message)
    } else {
      smsList[address] = Thread(message, address)
    }
  }

  /**
   * Return the complete list.
   */
  fun getThreadList(): List<Thread> {
    return smsList.values.toList().sortedByDescending { it.getShowDate() }
  }
}