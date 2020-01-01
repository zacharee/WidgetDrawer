package tk.zwander.widgetdrawer.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.WindowManager


val Context.canDrawOverlays
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(this)

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

fun Bitmap?.toBitmapDrawable(resources: Resources): BitmapDrawable? {
    return if (this != null) BitmapDrawable(resources, this) else null
}

fun Intent.ShortcutIconResource?.loadToDrawable(context: Context): Drawable? {
    return if (this != null) {
        context.packageManager.getResourcesForApplication(packageName)
            .run {
                getDrawable(getIdentifier(resourceName, "drawable", packageName))
            }
    } else {
        null
    }
}