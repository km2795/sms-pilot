package sr79.works.smspilot

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import sr79.works.smspilot.composables.DisplayThread
import java.net.URL

/**
 * ViewModel for the Landing Page of the SMS Pilot application.
 *
 * This ViewModel is responsible for managing the data related to SMS messages
 * and their display in the UI. It handles permission requests for reading SMS,
 * loading messages from the device and DataStore, organizing messages into threads,
 * and running spam prediction on messages.
 *
 * It uses a [DataStore] to persist SMS messages and permission status.
 * It observes changes in the SMS content provider to refresh the message list
 * automatically.
 *
 * The ViewModel exposes StateFlows for UI elements to observe, such as the list
 * of displayable SMS threads ([displayThreads]) and the visibility of the permission
 * button ([showPermissionButton]).
 *
 * @property application The application context, used for accessing system services
 *                       like ContentResolver and checking permissions.
 * @property dataStore An instance of [DataStore] used for storing and retrieving
 *                     SMS messages and permission status persistently.
 * @constructor Creates an instance of LandingPageViewModel with the provided
 *              application context and DataStore.
 */
class LandingPageViewModel(
  private val application: Application,
  private val dataStore: DataStore
): ViewModelProvider.Factory, AndroidViewModel(application) {

  /**
   * Holds the list of SMS threads to be displayed in the UI.
   * Returns a list of [DisplayThread] objects.
   */
  private val _displayThreads = MutableStateFlow<List<DisplayThread>>(emptyList())

  /**
   * A StateFlow that emits the list of displayable SMS threads.
   * This property provides an observable stream of `List<DisplayThread>` objects.
   */
  val displayThreads: StateFlow<List<DisplayThread>> = _displayThreads.asStateFlow()

  /**
   * Stores all loaded messages, indexed by their unique ID.
   */
  private var messageIndex: MutableMap<Long, Message> = mutableMapOf()

  /**
   * For controlling visibility of the permission button.
   */
  private val _showPermissionButton = MutableStateFlow(true)

  /**
   * A StateFlow that indicates whether the permission button should be shown.
   */
  val showPermissionButton: StateFlow<Boolean> = _showPermissionButton.asStateFlow()

  /**
   * Updates the visibility of the permission button.
   *
   * If `show` is true, the permission button will be displayed,
   * and any existing SMS messages in the DataStore will be cleared.
   * This is typically done when the app needs to re-request SMS permission.
   *
   * If `show` is false, the permission button will be hidden.
   * This is usually done after the permission has been granted.
   *
   * @param show A boolean indicating whether to show (true) or hide (false) the permission button.
   */
  fun updateShowPermissionButton(show: Boolean) {
    _showPermissionButton.value = show
    if (show) {
      clearSmsMessages()
    }
  }

  /**
   * Handle for content resolver. (Used specifically for content observer).
   */
  private val contentResolver = application.contentResolver

  /**
   * Observes changes in the SMS content provider.
   */
  private val messageObserver = object: ContentObserver(Handler(Looper.getMainLooper())) {
    override fun onChange(selfChange: Boolean, uri: Uri?) {
      super.onChange(selfChange, uri)

      // Update happens. Refresh the list.
      refreshMessageList()
    }
  }

  /**
   * Register an observer for the SMS content provider,
   * and kick start the functionality of the app.
   */
  init {
    contentResolver.registerContentObserver(
      Telephony.Sms.CONTENT_URI,
      true,
      messageObserver
    )

    // Where the App starts it's functionality.
    load()
  }

  /**
   * Checks for the READ_SMS permission. If the permission is already granted,
   * it updates the DataStore, hides the permission button, and loads the SMS
   * threads. If the permission is not granted, it updates the DataStore to
   * reflect this, ensures the permission button is visible, and then launches
   * the permission request using the provided [activityResultLauncher].
   *
   * @param activityResultLauncher Used to request the SMS read permission.
   */
  fun checkAndRequestPermission(activityResultLauncher: ActivityResultLauncher<String>) {
    val context = getApplication<Application>().applicationContext
    when {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
      ) == PackageManager.PERMISSION_GRANTED -> {
        dataStore.updateSmsReadPermission(context, true)
        _showPermissionButton.value = false
        loadThreads(true)
      }
      else -> {
        // Update DataStore to reflect that permission is not (yet) granted
        dataStore.updateSmsReadPermission(context, false)
        _showPermissionButton.value = true // Ensure button is shown while asking
        activityResultLauncher.launch(Manifest.permission.READ_SMS)
      }
    }
  }

  /**
   * Handles the result of the SMS read permission request.
   *
   * Updates the DataStore with the permission status and
   * controls the visibility of the permission button. If
   * permission is granted, it loads the SMS threads. If
   * permission is denied, it clears the SMS messages.
   *
   * @param isGranted
   */
  fun handlePermissionResult(isGranted: Boolean) {
    val context = getApplication<Application>().applicationContext
    dataStore.updateSmsReadPermission(context, isGranted)
    _showPermissionButton.value = !isGranted
    if (isGranted) {
      loadThreads(true)
    } else {
      clearSmsMessages() // Or handle denial appropriately
    }
  }

  /**
   * Check if Read permission is already stored.
   * Load the list as per permission.
   */
  private fun load() {
    viewModelScope.launch {

      // Check permission from data store.
      val permission = checkPermission()

      /*
       * In case user disables the permission or the system revokes the
       * permission outside the app. This would potentially render the
       * app to crash. Adding a check at the start to see if the permission
       * in the data store is true and system-wide permission is false.
       * If so, re-orient the app to the permission screen.
       */
      if (permission
        &&
        (ContextCompat.checkSelfPermission(
          application,
          Manifest.permission.READ_SMS
        ) != PackageManager.PERMISSION_GRANTED)
      ) {
        // Show the permission button.
        _showPermissionButton.value = true
        clearSmsMessages()
      } else if (permission) {
        // Load the Threads.
        _showPermissionButton.value = false

        loadThreads(true)
      } else {

        // In case, permission was stored.
        _showPermissionButton.value = true
      }
    }
  }

  /**
   * Checks the stored SMS read permission status.
   *
   * Retrieves the SMS read permission status that was previously
   * stored in the [DataStore].
   *
   * @return `true` if the permission was previously granted and stored as such,
   *         `false` otherwise (including if the permission status has not yet
   *         been stored).
   */
  private fun checkPermission(): Boolean {
    return dataStore.getSmsReadPermission(application)
  }

  /**
   * Loads messages from the DataStore into the `messageIndex`.
   *
   * This function retrieves the list of messages stored in the DataStore.
   * If the list is not null, it iterates through each message and adds it
   * to the `messageIndex` map, using the message's ID as the key.
   *
   * @return The number of messages loaded into the `messageIndex`.
   *         Returns 0 if no messages were loaded (e.g., if the DataStore
   *         returned a null list).
   */
  private fun loadMessageIndex(): Int {
    var messageList = dataStore.loadMessageList() ?: return 0

    messageList.forEach { message ->
      messageIndex[message.getId()] = message
    }
    return messageIndex.size
  }

  /**
   * Return the list of message Ids that are not added to the list.
   */
  private fun getUniqueMessages(): List<Message> {

    var uniqueMessages: MutableList<Message> = mutableListOf()

    /*
     * Fetch messages from content provider
     * using 'AppHandler.getMessageList()'
     * and then for each item, check in the
     * 'messageIndex' for unique message.
     */
    AppHandler.getMessageList(application.contentResolver).forEach { item->
      if (messageIndex[item.getId()] == null) {
        messageIndex[item.getId()] = item
        dataStore.storeMessage(item)
        uniqueMessages.add(item)
      }
    }

    return uniqueMessages
  }

  /**
   * Loads the SMS threads and updates the UI.
   *
   * It retrieves the current list of messages from `messageIndex`,
   * groups them into threads using `AppHandler.getThreadList`,
   * and then converts these threads into a displayable format
   * using `AppHandler.getDisplayThreads`. The resulting list of
   * `DisplayThread` objects is then assigned to `_displayThreads`,
   * which triggers an update in the UI observing this StateFlow.
   */
  private fun loadThreads(initial: Boolean) {
    viewModelScope.launch(Dispatchers.Default) {
      /* When there is no message in the DataStore. */
      if (loadMessageIndex() < 1) {
        getUniqueMessages()
      }
      _displayThreads.value = AppHandler.getDisplayThreads(
        AppHandler.getThreadList(
          messageIndex.values.toList()
        )
      )
      if (initial) {
        runPredictor(messageIndex.values.toList())
      }
    }
  }

  /**
   * Refreshes the message list if there are any changes.
   *
   * This function is triggered when a change is detected
   * in the SMS content provider. It checks for a difference
   * in the number of messages between the current `messageIndex`
   * and the messages in the content resolver. If a difference
   * is found, it reloads the threads to update the UI.
   */
  fun refreshMessageList() {
    viewModelScope.launch {
      var uniqueMessages = getUniqueMessages()

      // Run the predictor, only if there are new messages.
      if (uniqueMessages.isNotEmpty())
        runPredictor(uniqueMessages)
      loadThreads(false)
    }
  }

  /**
   * To clear the message list.
   */
  private fun clearSmsMessages() {
    messageIndex.clear()
    dataStore.clearTable()
  }

  /**
   * Runs the spam prediction API for a list of messages.
   *
   * This function iterates through the provided list of messages. If a message's
   * spam status has not been previously determined (i.e., `getSpamOrNot()` returns null),
   * it calls the `predictorApi` to get a spam verdict. If a verdict is received,
   * the message's spam status is updated, and the message is updated in the DataStore.
   *
   * Payload Structure.
   * {
   *  size: #,
   *  messages: {
   *    <id>: <message>
   *  }
   * }
   *
   * If the `API_URL` is empty, this function does nothing.
   * If the `messages` parameter is null, it defaults to processing all messages
   * currently in the `messageIndex`.
   *
   * @param messageList A list of [Message] objects to be processed. If null, all messages
   *                 in `messageIndex` will be processed.
   */
  suspend fun runPredictor(messageList: List<Message>?) {
    if (messageList.isNullOrEmpty()) return;

    // Designate either of the two available Predictor API service.
    val apiUrl = if (PREDICTOR_API_URL.isNotEmpty()) {
      PREDICTOR_API_URL
    } else {
      SECONDARY_PREDICTOR_API_URL
    }

    if (apiUrl.isEmpty()) {
      Log.w("Predictor API URL", "No predictor API URL configured.")
      return;
    }

    var isPredictorApiAvailable = false
    while (!isPredictorApiAvailable) {
      try {
        if (apiUrl.isNotEmpty()) {

          var payload = JSONObject()
          var payloadSize = 0
          var messagesJsonObject = JSONObject()

          for (message in messageList) {
            // If the message is not already checked. Push it into the list.
            if (message.getSpamOrNot() == null) {
              messagesJsonObject.put(message.getId().toString(), message.getBody())
              payloadSize++
            }
          }
          payload.put("size", payloadSize)
          payload.put("messages", messagesJsonObject)

          // Send the payload for verdict.
          var verdictPayload = predictorApi(payload, URL(apiUrl))
          if (verdictPayload != null) {
            for (message in messageList) {
              var tempVerdict = verdictPayload.get(message.getId().toString())

              // Only if the verdict has been received, should the spam status be updated,
              // otherwise keep it null.
              if (tempVerdict == "Spam") {
                message.setSpamOrNot(true)
              } else if (tempVerdict == "Not Spam") {
                message.setSpamOrNot(false);
              }

              // Update the message in store.
              dataStore.updateMessage(message)
            }
          }

          // Update the UI.
          loadThreads(false)
          isPredictorApiAvailable = true
        }
      } catch (e: Exception) {
        Log.e("Predictor API Service", "Prediction Service Unavailable. Retrying in 10 seconds.", e)
        isPredictorApiAvailable = false
        delay(10000)
      }
    }
  }

  /**
   * Factory class for creating instances of [LandingPageViewModel].
   *
   * This factory is responsible for providing the necessary dependencies
   * to the [LandingPageViewModel] during its construction.
   *
   * @property application The application context.
   * @property dataStore An instance of [DataStore] for accessing persistent data.
   */
  class LandingPageViewModelFactory(
    private val application: Application,
    private val dataStore: DataStore
  ): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(LandingPageViewModel::class.java)) {
        return LandingPageViewModel(application, dataStore) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}