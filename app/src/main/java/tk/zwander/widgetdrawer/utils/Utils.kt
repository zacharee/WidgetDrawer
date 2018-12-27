package tk.zwander.widgetdrawer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.TypedValue
import android.view.WindowManager


val Context.canDrawOverlays
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(this)

fun Context.dpAsPx(dpVal: Int) =
    dpAsPx(dpVal.toFloat())

fun Context.dpAsPx(dpVal: Float) =
    Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, resources.displayMetrics))

fun Context.pxAsDp(pxVal: Int) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pxVal.toFloat(), resources.displayMetrics)

fun Context.screenSize(): Point {
    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    return Point().apply { display.getRealSize(this) }
}

fun Context.vibrate(len: Long) {
    val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
        val effect = VibrationEffect.createOneShot(len, VibrationEffect.DEFAULT_AMPLITUDE)
        vib.vibrate(effect)
    } else {
        vib.vibrate(len)
    }
}

fun Drawable.toBitmap(): Bitmap? {
    return when (this) {
        is BitmapDrawable -> bitmap
        else -> {
            (if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            })?.apply {
                val canvas = Canvas(this)
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
            }
        }
    }
}
