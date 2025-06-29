package sr79.works.smspilot

import android.app.Activity
import android.content.res.AssetFileDescriptor
import android.util.Log
import org.tensorflow.lite.Interpreter
import sr79.works.smspilot.detector.HashingVectorizer
import sr79.works.smspilot.detector.TextPreprocessor
import java.io.FileInputStream
import java.io.IOException
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

fun runInference(model: MappedByteBuffer, inputData: String): Float {
  // Initialize the interpreter.
  val interpreter = Interpreter(model)

  // Process the input data.
  val cleaned = TextPreprocessor.processWord(true, false, inputData, "\n") as String

  // Parameterize the Vectorizer.
  val vectorizer = HashingVectorizer(200)

  // Transform the input data into a vector.
  val vectorizedData: DoubleArray = vectorizer.transform(listOf(cleaned))

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

    return verdict
  } catch (e: Exception) {
    Log.i("ModelError: ", e.printStackTrace().toString())

    // Model error occurred.
    return -1f
  }
}

/**
 * Wrapper function for the runInference.
 *
 * @param modelFile Model file.
 * @param inputData Input data.
 * @return Verdict Boolean
 */
fun spamOrNot(modelFile: MappedByteBuffer?, inputData: String): Boolean {
  if (modelFile == null) {
    return false
  }

  val verdict = runInference(modelFile, inputData)

  // In case of error.
  return if (verdict > 0.5f) {
    true
  } else {
    false
  }
}