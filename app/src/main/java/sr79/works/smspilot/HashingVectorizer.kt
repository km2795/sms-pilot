package sr79.works.smspilot

import java.util.Arrays
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * Hashing Vectorizer
 * Based on Python's Scikit Library
 */
class HashingVectorizer(private val nFeatures: Int = 2.0.pow(8.0).toInt(),
                        private val norm: String = "l2",
                        private val binary: Boolean = false,
                        private val alternateSign: Boolean = true
    ) {
    var input = ""
    var encoding = "UTF-8"
    var decodeError = "strict"
    var stripAccents: String? = null
    var lowercase = true
    var tokenPattern = "\\b\\w\\w+\\b"
    var ngramRange = intArrayOf(1,1)
    var analyzer = "word"

    fun fit(item: List<String>): DoubleArray {
        return fitTransform(item)
    }

    fun fitTransform(X: List<String>): DoubleArray {
        return transform(X)
    }

    /**
     * Transform text data into vectorized format.
     */
    fun transform(item: List<String>): DoubleArray {
        require(!(item == null || item.isEmpty())) { "Iterable over raw text documents expected." }

        val vectorizedData = DoubleArray(nFeatures) // 1D array

        for (doc in item) {
            val hash = abs((doc.hashCode() % nFeatures).toDouble()).toInt()
            val value =
                (if (alternateSign) sign(hash.toDouble()) * abs(hash.toDouble())
                else abs(hash.toDouble()).toFloat()).toDouble()
            vectorizedData[hash] += if (binary) 1.0 else value // Accumulate feature values
        }

        // Apply normalization (l1 or l2)
        if ("l1" == norm) {
            val sum = Arrays.stream(vectorizedData).sum()
            if (sum != 0.0) {
                for (j in 0..<nFeatures) {
                    vectorizedData[j] /= sum
                }
            }
        } else if ("l2" == norm) {
            val sumSquared = Arrays.stream(vectorizedData).map { v: Double -> v * v }.sum()
            if (sumSquared != 0.0) {
                for (j in 0..<nFeatures) {
                    vectorizedData[j] /= sqrt(sumSquared)
                }
            }
        }

        return vectorizedData
    }
}