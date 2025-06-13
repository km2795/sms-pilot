package com.example.smspilot

import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Loads the model from the cache.
 *
 * @param activity  Activity context.
 * @param modelPath  Model's path in directory.
 */
fun loadModelFile(activity: Activity, modelPath: String): MappedByteBuffer? {
  // Try to load the model.
  try {
    val assetFileDescriptor: AssetFileDescriptor = activity.assets.openFd(modelPath)
    val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
    val fileChannel: FileChannel = inputStream.channel
    val startOffset: Long = assetFileDescriptor.startOffset
    val declaredLength: Long = assetFileDescriptor.declaredLength

    // Return the model as MappedByteBuffer.
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
  } catch (e: IOException) {
    // Log the error message.
    Log.i("Error: ", e.printStackTrace().toString())

    return null
  }
}

fun runInference(model: MappedByteBuffer?, inputData: String): String {
  if (model == null) {
    return "No Model Found!!"
  }

  // Initialize the interpreter.
  val interpreter = Interpreter(model)

  // Process the input data.
  val cleaned = TextPreprocessor.processWord(true, false, inputData, "\n") as String

  // Parameterize the Vectorizer.
  val vectorizer = HashingVectorizer(200)

  // Transform the input data into a vector.
  val vectorizedData: DoubleArray = vectorizer.fit(listOf(cleaned))

  // Convert the vectorized data into a feed-able format for the model.
  val vectorizedFloatData = vectorizedData.map { it.toFloat() }.toFloatArray()

  // Input to the model.
  val inputTensor = vectorizedFloatData

  // Set the output structure from the model.
  val outputTensor = Array(1) { FloatArray(1) }

  try {
    // Run the interpreter.
    interpreter.run(inputTensor, outputTensor)

    // Process the output
    val verdict: Float = outputTensor[0][0]

    // Return the verdict from the model.
    return if (verdict > 0.5) {
      "Verdict: Spam"
    } else {
      "Verdict: Not Spam"
    }
  } catch (e: Exception) {
    Log.i("ModelError: ", e.printStackTrace().toString())

    // Model error occurred.
    return("Some Error Occurred!!")
  }
}