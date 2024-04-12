package com.bytecause.lenslex.util

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import coil.size.Size
import coil.transform.Transformation
import jp.wasabeef.blurry.Blurry

class BlurTransformation(
    private val context: Context, private val radius: Int,
    override val cacheKey: String = "blur"
) : Transformation {

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val imageView = ImageView(context)
        imageView.setImageBitmap(input)

        // Apply blur effect using Blurry library
        Blurry.with(context)
            .radius(radius)
            .from(input)
            .into(imageView)

        // Get the blurred bitmap from the ImageView
        val blurredBitmap = imageView.drawable.toBitmap(
            input.width,
            input.height,
            Bitmap.Config.ARGB_8888
        )

        // Clean up the ImageView
        imageView.setImageBitmap(null)
        imageView.clearColorFilter()

        return blurredBitmap
    }
}