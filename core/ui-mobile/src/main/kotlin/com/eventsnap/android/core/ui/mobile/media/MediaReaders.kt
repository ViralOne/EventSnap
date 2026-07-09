package com.eventsnap.android.core.ui.mobile.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream

/**
 * Turns a picked/captured content Uri into JPEG bytes the AI layer can send to Groq.
 * Images are read straight through; PDFs are rendered (first page) to a bitmap first, since
 * the vision model takes images, not PDFs.
 */
object MediaReaders {
    private const val PDF_RENDER_WIDTH = 1240 // ~150 dpi A4 width, enough for OCR
    private const val JPEG_QUALITY = 90

    /** Returns non-empty JPEG bytes for [uri], or null if it can't be read or is empty (0 KB). */
    fun readAsJpeg(
        context: Context,
        uri: Uri,
    ): ByteArray? {
        val type = context.contentResolver.getType(uri).orEmpty()
        val bytes =
            if (type == "application/pdf" || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                renderPdfFirstPage(context, uri)
            } else {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
        // Reject empty files — a 0 KB image/PDF has nothing for the AI to read.
        return bytes?.takeIf { it.isNotEmpty() }
    }

    private fun renderPdfFirstPage(
        context: Context,
        uri: Uri,
    ): ByteArray? {
        val descriptor: ParcelFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r") ?: return null
        return descriptor.use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                renderer.takeIf { it.pageCount > 0 }?.openPage(0)?.use { page ->
                    val ratio = page.height.toFloat() / page.width.toFloat()
                    val height = (PDF_RENDER_WIDTH * ratio).toInt().coerceAtLeast(1)
                    val bitmap = createBitmap(PDF_RENDER_WIDTH, height)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap.toJpegBytes()
                }
            }
        }
    }

    private fun Bitmap.toJpegBytes(): ByteArray =
        ByteArrayOutputStream().use { stream ->
            compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            stream.toByteArray()
        }
}
