package com.example.smspilot;

import kotlin.math.abs

class HashingVectorizer(private val nFeatures: Int) {
    fun transform(text: String): DoubleArray {
        var vector = DoubleArray(nFeatures) { 0.0 }
        var tokens = text.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (token in tokens) {
            val hash = abs(token.hashCode())
            val idx = hash % nFeatures
            vector[idx] += 1.0
        }
        return vector
    }
}