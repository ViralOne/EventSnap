package com.eventsnap.android.core.model

/**
 * What the user handed to the app to be turned into events: either a free-text description
 * or an image (as raw bytes) with an optional MIME hint. The AI layer picks a text vs. vision
 * model based on which arm this is.
 */
sealed interface CaptureInput {
    data class Text(
        val description: String,
    ) : CaptureInput

    data class Image(
        val bytes: ByteArray,
        val mimeType: String = "image/jpeg",
    ) : CaptureInput {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Image) return false
            return bytes.contentEquals(other.bytes) && mimeType == other.mimeType
        }

        override fun hashCode(): Int = 31 * bytes.contentHashCode() + mimeType.hashCode()
    }
}
