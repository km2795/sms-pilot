package sr79.works.smspilot

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LandingPageViewModel(private val application: Application): AndroidViewModel(application) {

  /*
   * Private data objects for the view model.
   */
  private val _threadList = MutableStateFlow<List<Thread>>(emptyList())
  private val _threadListMap = MutableStateFlow<HashMap<String, Thread>>(HashMap())
  private val _messageList = MutableStateFlow<List<Message>>(emptyList())

  /*
   * Public modes of the view model's data objects.
   */
  val threadList: StateFlow<List<Thread>> = _threadList
  val threadListMap: StateFlow<HashMap<String, Thread>> = _threadListMap
  val messageList: StateFlow<List<Message>> = _messageList

  // Handle for content resolver. (Used specifically for content observer).
  private val contentResolver = application.contentResolver

  // For the content change. (For updates in the SMSs content provider).
  private val messageObserver = object: ContentObserver(Handler(Looper.getMainLooper())) {
    override fun onChange(selfChange: Boolean, uri: Uri?) {
      super.onChange(selfChange, uri)

      // Update happens. Refresh the list.
      refreshSmsMessages()
    }
  }

  init {
    contentResolver.registerContentObserver(
      Telephony.Sms.CONTENT_URI,
      true,
      messageObserver
    )
  }

  /**
   * For initial loading of the message list.
   */
  fun loadSmsMessages() {
    viewModelScope.launch {
      _messageList.value = APP.DATA_STORE_HANDLE?.loadMessageList() ?: AppHandler.getSmsList(contentResolver)
    }
  }

  /**
   * To refresh the message list with changes.
   */
  fun refreshSmsMessages() {
    // Should fetch and check for updates.
    viewModelScope.launch {
      _messageList.value = APP.DATA_STORE_HANDLE?.loadMessageList() ?: AppHandler.getSmsList(contentResolver)
    }
  }

  /**
   * To clear the message list.
   */
  fun clearSmsMessages() {
    _messageList.value = emptyList()
    APP.DATA_STORE_HANDLE?.clearTable()
  }
}