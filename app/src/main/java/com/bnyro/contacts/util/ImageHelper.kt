package com.bnyro.contacts.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object ImageHelper {
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
