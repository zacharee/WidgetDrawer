package tk.zwander.widgetdrawer.utils

import android.content.Context
import android.graphics.Point
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
