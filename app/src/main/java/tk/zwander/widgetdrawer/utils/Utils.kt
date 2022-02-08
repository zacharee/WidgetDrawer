package tk.zwander.widgetdrawer.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import com.arasthel.spannedgridlayoutmanager.SpanSize
import tk.zwander.helperlib.dpAsPx
import tk.zwander.widgetdrawer.App
import java.io.ByteArrayOutputStream
import java.io.IOException

var accessibilityConnected = false

fun View.calculateWidgetWidth(frameWidth: Int, size: SpanSize?): Int {
    return frameWidth / context.prefs.columnCount *
            (size?.width ?: 1)
}

fun View.calculateWidgetHeight(size: SpanSize?): Int {
    return (size?.height ?: 1) * context.widgetHeightUnit
}

val Context.widgetHeightUnit: Int
    get() = dpAsPx(50)

val Context.canDrawOverlays
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(this)

val Context.app: App
    get() = applicationContext as App

val Context.accessibilityEnabled: Boolean
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?.contains(packageName) == true

val Context.statusBarHeight: Int
    get() = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))

val Context.prefs: PrefsManager
    get() = PrefsManager.getInstance(this)

fun getProperWLPType(): Int {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    else WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
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

fun Context.pxAsDp(pxVal: Number) =
    pxVal.toFloat() / resources.displayMetrics.density

fun Bitmap?.toBitmapDrawable(resources: Resources): BitmapDrawable? {
    return if (this != null) BitmapDrawable(resources, this) else null
}

fun Intent.ShortcutIconResource?.loadToDrawable(context: Context): Drawable? {
    return if (this != null) {
        try {
            context.packageManager.getResourcesForApplication(packageName)
                .run {
                    ResourcesCompat.getDrawable(this, getIdentifier(resourceName, "drawable", packageName), newTheme())
                }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    } else {
        null
    }
}

fun Bitmap?.toByteArray(): ByteArray? {
    if (this == null) return null

    val size: Int = width * height * 4
    val out = ByteArrayOutputStream(size)
    return try {
        compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        out.toByteArray()
    } catch (e: IOException) {
        Log.w("WidgetDrawer", "Could not write bitmap")
        null
    }
}

fun Bitmap?.toBase64(): String? {
    return toByteArray()?.toBase64()
}

fun ByteArray?.toBase64(): String? {
    if (this == null) return null

    return Base64.encodeToString(this, 0, size, Base64.DEFAULT)
}

fun ByteArray?.toBitmap(): Bitmap? {
    if (this == null) return null

    return BitmapFactory.decodeByteArray(this, 0, size)
}

fun String?.base64ToByteArray(): ByteArray? {
    if (this == null) return null

    return Base64.decode(this, Base64.DEFAULT)
}

fun String?.base64ToBitmap(): Bitmap? {
    return base64ToByteArray()?.toBitmap()
}