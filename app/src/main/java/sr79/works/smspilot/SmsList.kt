package sr79.works.smspilot

/**
 * Main SMS list.
 */
class SmsList {
  /**
   * Add message to a thread in the list.
   *
   * @param message Message to add.
   */
  fun addMessage(smsMap: MutableMap<String, Thread>, message: Message) {
    val address = message.getAddress()
    if (smsMap[address] != null) {
      smsMap[address]?.addMessage(message)
    } else {
      smsMap[address] = Thread(message, address)
    }
  }

//
//  fun addMessage(message: Message) {
//    val address = message.getAddress()
//
//    SMS_LIST.SMS_LIST_MAP.compute(address) { _key, existingThread ->
//      if (existingThread != null) {
//        existingThread.addMessage(message)
//        existingThread
//      } else {
//        Thread(message, address)
//      }
//    }
//  }

  /**
   * Return a thread from the list.
   *
   * @param address Address of the thread.
   */
  fun getThread(smsMap: MutableMap<String, Thread>, address: String): Thread? {
    return smsMap[address]
  }

  /**
   * Return the complete list.
   */
  fun getThreadList(smsMap: MutableMap<String, Thread>): MutableList<Thread> {
    return smsMap
      .values
      .toList()
      .sortedByDescending { it.getShowDate() }
      .toMutableList()
  }

  /**
   * Clear the lists.
   */
  fun clearList(smsMap: MutableMap<String, Thread>, smsList: MutableList<Thread>) {
    smsList.clear()
    smsMap.clear()
  }
}