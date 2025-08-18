package sr79.works.smspilot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sends SMS data to a remote API to get a spam verdict.
 * This function should be called from a coroutine.
 *
 * @param data The body of the SMS message.
 * @param apiUrl API URL for the spam detection service.
 * @return True if the message is considered spam, false otherwise, or null on error.
 */
suspend fun predictorApi(data: String, apiUrl: URL): Boolean? {
  // Use Dispatchers.IO for network operations
  return withContext(Dispatchers.IO) {
    try {
      // Replace with your actual API endpoint
      val urlConnection = apiUrl.openConnection() as HttpURLConnection

      // Set up the connection for a POST request
      urlConnection.requestMethod = "POST"
      urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
      urlConnection.setRequestProperty("Accept", "application/json")
      urlConnection.doOutput = true
      urlConnection.doInput = true

      // Create JSON payload
      val payload = JSONObject()
      payload.put("message", data)

      // Send the request
      val outputStreamWriter = OutputStreamWriter(urlConnection.outputStream)
      outputStreamWriter.write(payload.toString())
      outputStreamWriter.flush()
      outputStreamWriter.close()

      val responseCode = urlConnection.responseCode

      if (responseCode == HttpURLConnection.HTTP_OK) {
        val response = urlConnection.inputStream.bufferedReader().use { it.readText() }
        val jsonResponse = JSONObject(response)

        if (jsonResponse.has("verdict")) {
          var verdict = jsonResponse.get("verdict")
          if (verdict == "Spam") true
          else if (verdict == "Not Spam") false
          else null
        } else {
          // Handle cases where the expected field is missing
          System.err.println("API Error: 'verdict' field missing in response: $response")
          null
        }
      } else {
        val errorResponse =
          urlConnection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
        System.err.println("API Error: $responseCode - $errorResponse")
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
