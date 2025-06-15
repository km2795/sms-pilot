package com.example.smspilot

import java.util.concurrent.ConcurrentHashMap


object SMS_LIST {
  var SMS_LIST: MutableList<Thread> = mutableListOf()
  var SMS_LIST_MAP: MutableMap<String, Thread> = ConcurrentHashMap<String, Thread>()
}

/**
 * Main SMS list.
 */
class SmsList {

  /**
   * Add message to a thread in the list.
   *
   * @param message Message to add.
   */
//  fun addMessage(message: Message) {
//    val address = message.getAddress()
//    if (SMS_LIST.SMS_LIST_MAP[address] != null) {
//      SMS_LIST.SMS_LIST_MAP[address]?.addMessage(message)
//    } else {
//      SMS_LIST.SMS_LIST_MAP[address] = Thread(message, address)
//    }
//  }

  fun addMessage(message: Message) {
    val address = message.getAddress()

    SMS_LIST.SMS_LIST_MAP.compute(address) { _key, existingThread ->
      if (existingThread != null) {
        existingThread.addMessage(message)
        existingThread
      } else {
        Thread(message, address)
      }
    }
  }

  /**
   * Return a thread from the list.
   *
   * @param address Address of the thread.
   */
  fun getThread(address: String): Thread? {
    return SMS_LIST.SMS_LIST_MAP[address]
  }

  /**
   * Return the complete list.
   */
  fun getThreadList(): MutableList<Thread> {
    return SMS_LIST.SMS_LIST_MAP
      .values
      .toList()
      .sortedByDescending { it.getShowDate() }
      .toMutableList()
  }
}