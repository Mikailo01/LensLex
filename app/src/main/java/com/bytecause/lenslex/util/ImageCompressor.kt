package com.bytecause.lenslex.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


fun byteArrayToUri(context: Context, byteArray: ByteArray): Uri? {
    try {
        // Create a temporary file to store the image data
        val tempFile = File.createTempFile("temp_image", null, context.cacheDir)

        // Write the ByteArray data to the file
        FileOutputStream(tempFile).use { outputStream ->
            outputStream.write(byteArray)
        }

        // Return the Uri for the temporary file
        return Uri.fromFile(tempFile)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun compressImage(
    context: Context,
    imageUri: Uri,
    maxWidth: Int = 300,
    maxHeight: Int = 300,
    quality: Int
): Uri? {
    var inputStream: InputStream? = null
    try {
        // Open the input stream from the URI
        inputStream = context.contentResolver.openInputStream(imageUri)

        // Decode the input stream into a Bitmap
        val options = BitmapFactory.Options().apply {
            // Only decode the dimensions to save memory
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)

        // Calculate the inSampleSize (scaling factor) to achieve desired dimensions
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

        // Reset the input stream to start decoding again
        inputStream?.close()
        inputStream = context.contentResolver.openInputStream(imageUri)

        // Decode the input stream into a scaled Bitmap
        options.inJustDecodeBounds = false
        val scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options)

        // Compress the Bitmap into a byte array
        val outputStream = ByteArrayOutputStream()
        scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return byteArrayToUri(context = context, byteArray = outputStream.toByteArray())
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        inputStream?.close()
    }
    return null
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (width, height) = options.outWidth to options.outHeight
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}