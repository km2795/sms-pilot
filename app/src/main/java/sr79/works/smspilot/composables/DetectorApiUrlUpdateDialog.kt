package sr79.works.smspilot.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectorApiUrlUpdateInput(
  modifier: Modifier = Modifier,
  initialUrl: String = "",
  onSaveClicked: (newUrl: String) -> Unit
) {
  // State to hold the current text in the TextField
  var apiUrl by remember(initialUrl) { mutableStateOf(initialUrl) }

  // State to manage potential error message for the input
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items in Column
    ) {
      Text(
        text = "Enter Spam Detector API URL",
        style = MaterialTheme.typography.titleMedium
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = apiUrl,
        onValueChange = {
          apiUrl = it

          // Simple validation example: Clear error when user types
          if (errorMessage != null) {
            errorMessage = null
          }
        },
        label = { Text("API URL") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = errorMessage != null
      )

      if (errorMessage != null) {
        Text(
          text = errorMessage!!,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.fillMaxWidth()
        )
      } else {
        Spacer(modifier = Modifier.height(MaterialTheme.typography.bodySmall.lineHeight.value.dp * 2))
      }


      Spacer(modifier = Modifier.height(8.dp))

      Button(
        onClick = {
          // Basic validation example (you can make this more robust)
          if (apiUrl.isBlank() || !(apiUrl.startsWith("http://") || apiUrl.startsWith("https://"))) {
            errorMessage = "Please enter a valid URL (e.g., http://... or https://...)"
          } else {
            errorMessage = null // Clear any previous error
            onSaveClicked(apiUrl)
          }
        },
        modifier = Modifier.fillMaxWidth(0.6f) // Button takes 60% of the width
      ) {
        Text("Save")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DetectorApiUrlUpdateDialog(
  showDialog: Boolean = true, // Control visibility from parent
  onDismissRequest: () -> Unit = {},
  currentUrl: String = "http://example.com",
  onSave: (String) -> Unit = { println("Dialog Save: $it") }
) {
  if (showDialog) {
    Dialog(onDismissRequest = onDismissRequest) {
      // Use the input "box" composable inside the Dialog
      DetectorApiUrlUpdateInput(
        initialUrl = currentUrl,
        onSaveClicked = { newUrl ->
          onSave(newUrl)
        }
      )
    }
  }
}
