package com.mirage.reverie

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

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

fun Date.toLocalDate(): LocalDate {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun formatDate(date: Date, pattern: String = "dd MMMM yyyy", locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(date)
}

fun isUserAuthenticated(): Boolean {
    return FirebaseAuth.getInstance().currentUser != null
}

fun logout() {
    FirebaseAuth.getInstance().signOut()
}

fun getUserId() : String {
    return FirebaseAuth.getInstance().uid.toString()
}

