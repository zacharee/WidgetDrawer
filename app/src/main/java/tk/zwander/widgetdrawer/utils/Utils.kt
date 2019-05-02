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

val Context.canUseHiddenApis: Boolean
    get() {
        val underP = Build.VERSION.SDK_INT < Build.VERSION_CODES.P
        val underQ = Build.VERSION.SDK_INT < 29

        val pPolicy = Settings.Global.getString(contentResolver, "hidden_api_policy_p_apps")
        val qPolicy = Settings.Global.getString(contentResolver, "hidden_api_policy")

        val pAllowed = pPolicy == "0" || pPolicy == "1"
        val qAllowed = qPolicy == "0" || qPolicy == "0"

        return underP || if (underQ) pAllowed else qAllowed
    }

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
