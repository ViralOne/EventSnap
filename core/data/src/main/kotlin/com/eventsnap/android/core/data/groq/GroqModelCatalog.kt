package com.eventsnap.android.core.data.groq

/**
 * Default Groq model ids for each capture arm. These are free-tier models on Groq at time of
 * scaffolding; swap here if Groq renames them. Text uses a fast general model, images use a
 * vision-capable model that can OCR posters/tickets/screenshots.
 */
object GroqModelCatalog {
    const val TEXT_MODEL: String = "llama-3.3-70b-versatile"
    const val VISION_MODEL: String = "meta-llama/llama-4-scout-17b-16e-instruct"
}
