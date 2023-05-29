package com.bnyro.contacts.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

object ImageHelper {
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun getImageFromUri(context: Context, uri: Uri): Bitmap? {
        return getImageWithExif(context, uri) ?: getImageNormal(context, uri)
    }

    private fun getImageNormal(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

    private fun getImageWithExif(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            val exifInterface = ExifInterface(stream)
            val bitmap = exifInterface.thumbnailBitmap ?: return null
            rotateBitmap(bitmap, exifInterface)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, exifInterface: ExifInterface): Bitmap {
        val orientation = runCatching {
            exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrElse { return bitmap }

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> rotateImage(
                bitmap,
                90f
            )
            ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> rotateImage(
                bitmap,
                180f
            )
            ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> rotateImage(
                bitmap,
                -90f
            )
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, rotation: Float): Bitmap {
        val matrix = Matrix().apply {
            setScale(-1f, 1f)
            setRotate(rotation)
        }
        val oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return oriented
    }
}
