package sr79.works.smspilot

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sr79.works.smspilot.composables.DisplayThread
import java.nio.MappedByteBuffer

class LandingPageViewModel(
  private val application: Application,
  private val dataStore: DataStore,
  private val detector: MappedByteBuffer?
): ViewModelProvider.Factory, AndroidViewModel(application) {

  /*
   * Private data objects for the view model.
   */
  private val _threadList = MutableStateFlow<List<Thread>>(emptyList())
  private val _displayThreads = MutableStateFlow<List<DisplayThread>>(emptyList())
  private val _messageList = MutableStateFlow<List<Message>>(emptyList())

  /*
   * Public modes of the view model's data objects.
   */
  val threadList: StateFlow<List<Thread>> = _threadList.asStateFlow()
//  val displayThreads: StateFlow<List<DisplayThread>> = _displayThreads.asStateFlow()
  val messageList: StateFlow<List<Message>> = _messageList

  // For controlling visibility of the permission button.
  private val _showPermissionButton = MutableStateFlow(true)
  val showPermissionButton: StateFlow<Boolean> = _showPermissionButton.asStateFlow()

  val displayThreads: Flow<PagingData<DisplayThread>> = Pager(
    config = PagingConfig(
      pageSize = 30,
      enablePlaceholders = false
    ),
    pagingSourceFactory = { ThreadPagingSource(detector, application.contentResolver, application) }
  ).flow.cachedIn(viewModelScope)

  fun updateShowPermissionButton(show: Boolean) {
    _showPermissionButton.value = show
    if (show) {
      clearSmsMessages()
    }
  }

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
        loadThreads()
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
      loadThreads()
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
        loadThreads()
      } else {

        // In case, permission was stored.
        _showPermissionButton.value = true
      }
    }
  }

  private fun checkPermission(): Boolean {
    // Check the permission (if stored already).
    return dataStore.getSmsReadPermission(application)
  }

  /**
   * Load the Threads.
   */
  private fun loadThreads() {
    viewModelScope.launch(Dispatchers.IO) {
      val threads = AppHandler.getThreadList(detector, application.contentResolver)
      _threadList.value = threads
      _displayThreads.value = AppHandler.getDisplayThreads(threads)
    }
  }

  /**
   * To refresh the message list with changes.
   */
  fun refreshSmsMessages() {
    // Should fetch and check for updates.
    viewModelScope.launch {
      _messageList.value =
        dataStore.loadMessageList()
          ?: AppHandler.getMessageList(detector, contentResolver)
    }
  }

  /**
   * To clear the message list.
   */
  private fun clearSmsMessages() {
    _messageList.value = emptyList()
    dataStore.clearTable()
  }

  // Factory for our LandingPageViewModel.
  class LandingPageViewModelFactory(
    private val application: Application,
    private val dataStore: DataStore,
    private val detector: MappedByteBuffer?
  ): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(LandingPageViewModel::class.java)) {
        return LandingPageViewModel(application, dataStore, detector) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }

  class ThreadPagingSource(
    private val detector: MappedByteBuffer?,
    private val contentResolver: android.content.ContentResolver,
    private val application: Application // If AppHandler needs application context
    // You might not need to pass the full list of displayThreads anymore
  ) : PagingSource<Int, DisplayThread>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DisplayThread> {
      // Permission check should ideally happen before attempting to load.
      // If permission is not granted, this source should probably return an empty list or an error.
      // However, the ViewModel now controls the UI state based on permission.
      // The Pager will only be active if permission is granted.

      // The key (page number) is not directly used by AppHandler.getThreadList
      // as it fetches all threads at once.
      // For true pagination, AppHandler.getThreadList would need to support offset/limit.
      // For now, we'll load all and the Paging library will handle displaying them.
      // This isn't "true" pagination from the source but paginates the already loaded list.
      // To make this a true paginated source, AppHandler.getThreadList would need modification.

      // Let's assume for now, we load all threads once and Paging library handles the "view" paging.
      // A more robust PagingSource would fetch data in chunks from the ContentResolver.
      val currentPage = params.key ?: 1 // Paging library uses 1-based indexing for pages by default with Pager
      val pageSize = params.loadSize

      return try {
        // Perform permission check before accessing content resolver
        if (ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.READ_SMS
          ) != PackageManager.PERMISSION_GRANTED
        ) {
          // If permission is not granted, return an empty page or an error.
          // This prevents crashes if the source tries to load without permission.
          return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
        }

        // Simulate pagination if AppHandler cannot do it:
        // 1. Fetch all threads (this is inefficient for large datasets but matches current AppHandler)
        val allThreads = AppHandler.getThreadList(detector, contentResolver)
        val allDisplayThreads = AppHandler.getDisplayThreads(allThreads)

        // 2. Calculate the sublist for the current page
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(allDisplayThreads.size)

        val pagedThreads = if (startIndex < allDisplayThreads.size) {
          allDisplayThreads.subList(startIndex, endIndex)
        } else {
          emptyList()
        }

        LoadResult.Page(
          data = pagedThreads,
          prevKey = if (currentPage == 1) null else currentPage - 1,
          nextKey = if (pagedThreads.isEmpty() || endIndex >= allDisplayThreads.size) null else currentPage + 1
        )
      } catch (e: Exception) {
        // Log the exception or handle it more gracefully
        LoadResult.Error(e)
      }
    }

    override fun getRefreshKey(state: PagingState<Int, DisplayThread>): Int? {
      return state.anchorPosition?.let { anchorPosition ->
        state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
          ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
      }
    }
  }
}