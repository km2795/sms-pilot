package sr79.works.smspilot

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/**
 * A utility object providing various helper functions for common tasks
 * such as serialization, deserialization, file I/O, date formatting,
 * and string manipulation.
 */
object Utilities {

  private val MonthNames = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  )

  /**
   * Serialize any object (Class/object should be @Serializable
   * annotated).
   *
   * @param value Value to serialize.
   * @return Serialized value.
   */
  inline fun <reified T> serialize(value: T): String {
    return Json.encodeToString(value)
  }

  /**
   * Deserialize any object (Class/object should be @Serializable
   * annotated).
   *
   * @param value Value to deserialize.
   * @return Deserialized value.
   */
  inline fun <reified T> deserialize(value: String): T? {
    return try {
      Json.decodeFromString<T>(value)
    } catch (e: Exception) {
      return null
    }
  }

  /**
   * Converts a string to a boolean.
   *
   * This function parses the input string and convert
   * it to a boolean value. It ignores case and considers
   * "true" as `true` and "false" as `false`. If the string
   * does not match either of these values (case-insensitive),
   * it returns `null`.
   *
   * @param value The string to convert.
   * @return `true` if the string is "true" (case-insensitive),
   *         `false` if the string is "false" (case-insensitive),
   *         `true` if the string is "1"
   *         `false` if the string is "0"
   *         `null` otherwise.
   */
  inline fun stringToBoolean(value: String): Boolean? {
    return when {
      value.lowercase() == "false" -> false
      value.lowercase() == "true" -> true
      value.lowercase() == "1" -> true
      value.lowercase() == "0" -> false
      else -> null
    }
  }

  /**
   * Write a file to the app's data store.
   *
   * @param context Context to access resources
   * @param fileName File to write
   * @param data Data to write
   * @return 0 if successful, 1 if failed
   */
  fun writeFile(context: Context, fileName: String, data: String): Int {
    val file = File(context.filesDir, fileName)
    try {
      FileOutputStream(file).use { outputStream ->
        outputStream.write(data.toByteArray())
      }
      return 0;
    } catch (e: IOException) {
      Log.i("Error", e.message.toString())
      return 1;
    }
  }

  /**
   * Read a file from the app's data store.
   *
   * @param context Context to access resources
   * @param file File to read
   * @return File contents or null (for some error)
   */
  fun readFile(context: Context, file: String): String? {
    val file = File(context.filesDir, file)
    try {
      FileInputStream(file).use { inputStream ->
        inputStream.bufferedReader().use {
          return it.readText()
        }
      }
    } catch (e: IOException) {
      Log.i("Error", e.message.toString())
      return null
    }
  }

  /**
   * Utility function to read files from raw resources.
   *
   * @param context  Context to access resources
   * @param resourceId  Resource ID to read
   */
  fun readFromRawResource(context: Context, resourceId: Int): String {
    val stringBuilder = StringBuilder()
    val resources = context.resources

    try {
      resources.openRawResource(resourceId).use { inputStream ->
        InputStreamReader(inputStream).use { inputStreamReader ->
          BufferedReader(inputStreamReader).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
              stringBuilder.append(line).append('\n')
            }
          }
        }
      }
    } catch (e: IOException) {
      Log.i("Error: ", e.printStackTrace().toString())
    }

    return stringBuilder.toString()
  }

  /**
   * Modifies a date string for display.
   *
   * @param dateStr The date string to parse. Can be a variety of formats parsable by LocalDateTime/Instant,
   *                or a timestamp in milliseconds.
   * @param full If true, returns a full date and time string (e.g., "01 Jan 2023 14:30").
   *             If false, returns a condensed format:
   *             - HH:mm if the date is today.
   *             - dd MMM if the date is within the current year.
   *             - dd MMM yyyy otherwise.
   * @return The formatted date string.
   */

  fun modifyDateField(dateStr: String?, full: Boolean): String {
    val currentDateTime = Calendar.getInstance()

    try {
      dateStr?.toLongOrNull()?.let {
        // Try parsing as a timestamp in milliseconds
        currentDateTime.time = Date(it)
      } ?: dateStr?.let {
        val formats = arrayOf(
          "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
          "yyyy-MM-dd'T'HH:mm:ss'Z'",
          "EEE, dd MMM yyyy HH:mm:ss Z"
        )

        for (format in formats) {
          try {
            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            currentDateTime.time = sdf.parse(it) ?: continue
            break
          } catch (e: Exception) {
            // Ignore and try next format
          }
        }
      }
    } catch (e: Exception) {
      // Parsing failed; fallback to current time
    }

    val today = Calendar.getInstance()

    return if (full) {
      val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH)
      formatter.format(currentDateTime.time)
    } else {
      val messageDate = Calendar.getInstance()
      messageDate.time = currentDateTime.time

      when {
        // If the date matches today's date, show hours and minutes
        messageDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                messageDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
          val timeFormatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
          timeFormatter.format(currentDateTime.time)
        }
        // If the date belongs to the current year, show only date and month
        messageDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
          val day = messageDate.get(Calendar.DAY_OF_MONTH)
          val month = messageDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)
          "$day $month"
        }
        // Otherwise, show date, month, and year
        else -> {
          val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
          dateFormatter.format(currentDateTime.time)
        }
      }
    }
  }

  /**
   * To check if the Thread's address has only number
   * or contains text too. (Return the first letter
   * of the address or # (in case of number only
   * address).
   *
   * @param text String to check
   * @return String to place in contact photo.
   */
  fun placeholderForContact(text: String): String {
    val numberPattern = "^(\\+\\d{1,3}[- ]?)?\\d+$".toRegex()

    // It's a number match.
    return if (text.matches(numberPattern)) {
      "#"
    } else {
      // Text match. (other than number).
      text.firstOrNull()?.uppercaseChar().toString()
    }
  }
}
