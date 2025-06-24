package sr79.works.smspilot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.nio.MappedByteBuffer

const val APP_TITLE = "SMS Pilot"
var DETECTOR: MappedByteBuffer? = null

class MainActivity : ComponentActivity() {

  // ViewModel for managing the SMS_LIST
  private val smsViewModel: SmsViewModel by viewModels()

  // For controlling visibility of the permission button.
  private var showPermissionButton by mutableStateOf(true)

  // ActivityResultLauncher for permission request
  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        showPermissionButton = false

        // Load through the ViewModel.
        smsViewModel.initialLoadSmsMessages(this.contentResolver)

        // Update the "READ YES" permission to the data store.
        DataStore().updateSmsReadPermission(this, true)
      } else {
        // User denied permission.
        showPermissionButton = true
        // Update the "READ NO" permission to the data store.
        DataStore().updateSmsReadPermission(this, false)

        // Not essentially required.
        smsViewModel.clearSmsMessages()
      }
    }

  private var onRequestPermission = {
    when {
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_SMS
      ) == PackageManager.PERMISSION_GRANTED -> {
        // Update the "READ YES" permission to the data store.
        DataStore().updateSmsReadPermission(this, true)

        // Load through ViewModel.
        smsViewModel.initialLoadSmsMessages(this.contentResolver)

        // Hide the permission button.
        showPermissionButton = false
      }
      else -> {
        // Update the "READ NO" permission to the data store.
        DataStore().updateSmsReadPermission(this, false)

        // Show the permission dialog.
        requestPermissionLauncher.launch(Manifest.permission.READ_SMS)

        // Show the permission button.
        showPermissionButton = true

        // Not needed essentially.
        smsViewModel.clearSmsMessages()
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val loadPermission = DataStore().getSmsReadPermission(this)

    /*
     * In case user disables the permission or the system revokes the
     * permission outside the app. This would potentially render the
     * app to crash. Adding a check at the start to see if the permission
     * in the data store is true and system-wide permission is false.
     * If so, re-orient the app to the permission screen.
     */
    if (
      loadPermission
      &&
      (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_SMS
      ) != PackageManager.PERMISSION_GRANTED)) {

      // Hide the permission button.
      showPermissionButton = true

    } else if (loadPermission) {
      showPermissionButton = false

      // Load through ViewModel.
      smsViewModel.initialLoadSmsMessages(this.contentResolver)
    }

    /*
     * Load the detector.
     */
    DETECTOR = SmsPilot().setupDetector(this)

    setContent {
      // Collect the SMS list from the ViewModel as a State.
      val smsListFromViewModel by smsViewModel.smsThreads.collectAsState()

      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        MainScreen(
          SmsPilot().formAndGetThreadList(smsListFromViewModel.toMutableList()),
          DETECTOR,
          showPermissionButton,
          onRequestPermission,
          onShowPermissionButton = { show ->
            showPermissionButton = show
            if (show) {
              smsViewModel.clearSmsMessages()
              SmsPilot().unLoadSmsList()
            }
          },
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
  smsList: List<Thread>,
  modelFile: MappedByteBuffer?,
  showPermissionButton: Boolean,
  onRequestPermission: () -> Unit,
  onShowPermissionButton: (Boolean) -> Unit,
  modifier: Modifier = Modifier) {

  // For controlling visibility of the extra top bar actions.
  var showExtraTopActionMenu by rememberSaveable { mutableStateOf(false) }

  Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    TopAppBar(
      title = { Text(APP_TITLE) },
      navigationIcon = {},
      actions = {
        TopBarActions(
          showMenu = showExtraTopActionMenu,
          onShowExtraTopActionMenu = { showExtraTopActionMenu = it },
          onShowPermissionButton,
          modifier = modifier
        )
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.hsv(270f, 0.4f, 0.3f),
        titleContentColor = Color.hsv(0f, 0f, 1f)
      ),
      modifier = Modifier.shadow(6.dp)
    )
    ThreadList(
      smsList,
      modelFile,
      showPermissionButton,
      onRequestPermission,
      onShowPermissionButton,
      modifier = Modifier
    )
  }
}

// Simple Composable to display the SMS list and a button to request permission
@Composable
fun ThreadList(smsList: List<Thread>,
               modelFile: MappedByteBuffer?,
               showPermissionButton: Boolean,
               onRequestPermission: () -> Unit,
               onShowPermissionButton: (Boolean) -> Unit,
               modifier: Modifier = Modifier) {

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (showPermissionButton) {
      Button(onClick = onRequestPermission) {
        Text("Load SMS Messages")
      }
      if (smsList.isEmpty()) {
        Text("No SMS messages found or permission not granted.",
          modifier = Modifier.padding(16.dp)
        )
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()

          /*
           * To let threads slide over the navigation,
           * otherwise, they'd be covered by the navigation.
           */
          .padding(WindowInsets.navigationBars.asPaddingValues())
          .verticalScroll(rememberScrollState())
      ) {
        // Load each Thread.
        smsList.forEach { sms ->
          ThreadCard(sms, modifier)
        }
      }

    }
  }
}

@Composable
fun TopBarActions(
  showMenu: Boolean,
  onShowExtraTopActionMenu: (Boolean) -> Unit,
  onShowPermissionButton: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  // Load the context.
  val context = LocalContext.current

  IconButton(onClick = { onShowExtraTopActionMenu(true) }) {
    Icon(
      imageVector = Icons.Filled.MoreVert,
      tint = Color.White,
      contentDescription = "More Options",
    )
  }
  DropdownMenu(
    expanded = showMenu,
    onDismissRequest = { onShowExtraTopActionMenu(false) }
  ) {
    DropdownMenuItem(
      text = { Text("Clear Permissions") },
      onClick = {
        // Update the "READ NO" permission to the data store.
        DataStore().updateSmsReadPermission(context, false)

        // Update the ThreadList Composable to hide the list and show the button.
        onShowPermissionButton(true)

        // Hide the extra actions menu.
        onShowExtraTopActionMenu(false)
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Filled.Clear,
          contentDescription = "Clear Permissions Icon",
          tint = Color.Black
        )
      }
    )
  }
}


@Composable
fun ThreadCard(sms: Thread, modifier: Modifier = Modifier) {
  val smsBody: String = sms.getBodyThumbnail()
  val context = LocalContext.current

  /*
   * Run the inference on the SMS body.
   * Temporarily Shut-off.
   */
  /*val verdict: String = runInference(modelFile, smsBody)*/

  ElevatedCard(
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    onClick = {
      val intent = Intent(context, ThreadActivity::class.java)
      intent.putExtra("THREAD_ID", sms.getAddress())
      context.startActivity(intent)
    },
    modifier = Modifier.padding(5.dp)
  ) {
    Row(modifier= Modifier.padding(10.dp)) {
      Box(modifier = Modifier) {
        Image(
          painter = painterResource(id = R.drawable.user_foreground),
          contentDescription = "Contact Image",
          contentScale = ContentScale.Crop,
          colorFilter = ColorFilter.tint(Color.DarkGray),
          modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .border(0.5.dp, Color.Black, CircleShape)
       )
      }
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            sms.getAddress(),
            modifier.padding(horizontal = 15.dp, vertical = 3.dp)
          )
          Text(
            Utilities.modifyDateField(sms.getShowDate().toString(), false),
            textAlign = TextAlign.Right,
            fontSize = 13.sp,
            modifier = Modifier
              .padding(horizontal = 15.dp, vertical = 3.dp)
              .weight(1f)
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            smsBody,
            modifier
              .padding(horizontal = 15.dp, vertical = 3.dp)
              .weight(8f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.weight(1f))
          Box(modifier = Modifier.weight(1f)) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Black, CircleShape)
            ) {
              Text(
                sms.getThreadSize().toString(),
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // Show the verdict of the message. (Spam or not).
    /*Text(verdict, fontWeight = FontWeight.Bold)*/
  }
}
