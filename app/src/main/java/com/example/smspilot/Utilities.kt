package com.example.smspilot

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import android.util.Log

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