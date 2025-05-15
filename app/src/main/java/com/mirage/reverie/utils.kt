package com.mirage.reverie

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

fun drawableToBitmap(context: Context, drawableResId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableResId)
        ?: throw IllegalArgumentException("Drawable not found for resource ID: $drawableResId")

    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)


    return bitmap
}

