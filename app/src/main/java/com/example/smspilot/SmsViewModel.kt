package com.example.smspilot

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import android.provider.Telephony
import android.net.Uri

class SmsViewModel(private val application: Application) : AndroidViewModel(application) {

  // Private MutableStateFlow to hold the list of SMS threads
  private val _smsThreads = MutableStateFlow<List<Message>>(emptyList())

  // Public StateFlow to expose the list to the UI (read-only)
  val smsThreads: StateFlow<List<Message>> = _smsThreads

  // To track initial load changes.
  private var hasLoadedInitialData = false

  // ContentResolver to access SMS (for specific use cases)
  // When ContentResolver generally updates the SMS list.
  private val contentResolver = application.contentResolver

  private val smsObserver = object: ContentObserver(Handler(Looper.getMainLooper())) {
    override fun onChange(selfChange: Boolean, uri: Uri?) {
      super.onChange(selfChange, uri)
      Log.d("SmsViewModel", "Sms ContentObserver triggered. URI: $uri. Refreshing List.")

      /*
       * This is very inefficient, but for now.
       * The app would probably not respond for big lists.
       * A simple work around.
       */
      clearSmsMessages()
      initialLoadSmsMessages(contentResolver)
    }
  }

  init {
    contentResolver.registerContentObserver(
      Telephony.Sms.CONTENT_URI,
      true,
      smsObserver
    )
    Log.d("SmsViewModel", "SMS ContentObserver registered.")
  }

  /**
   * Function to load SMS messages
   */
  fun initialLoadSmsMessages(contentResolver: ContentResolver) {
    if (hasLoadedInitialData) {
      Log.d("SmsViewModel", "Initial SMS Already Loaded. Skipping Load.")
      return
    }
    viewModelScope.launch {
      Log.d("SmsViewModel", "Performing Initial SMS load.")
      hasLoadedInitialData = true
      _smsThreads.value = SmsPilot().getSmsList(contentResolver)
    }
  }

  /**
   * Function to refresh the SMS list.
   */
  fun refreshSmsMessages() {
    viewModelScope.launch {
      Log.d("SmsVieModel", "Refresh SMS List.")
      _smsThreads.value = SmsPilot().getSmsList(contentResolver)
    }
  }

  /**
   * Function to handle SMS deletion.
   * Specific Thread deletion might be implemented in the future.
   * For now, simple complete list reload is sufficient.
   */
  fun handleSmsDeleted() {
    refreshSmsMessages()
  }

  /**
   * Function to clear the SMS list (e.g., when permissions are revoked)
   */
  fun clearSmsMessages() {
    _smsThreads.value = emptyList()
    hasLoadedInitialData = false
    SmsPilot().unLoadSmsList() // here if needed
  }

  /**
   * Unregister the observer when the ViewModel is destroyed.
   */
  override fun onCleared() {
    contentResolver.unregisterContentObserver(smsObserver)
    Log.d("SmsViewModel", "SMS ContentObserver unregistered.")
    super.onCleared()
  }
}