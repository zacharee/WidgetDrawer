package tk.zwander.widgetdrawer.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Process
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
