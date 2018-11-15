package tk.zwander.widgetdrawer.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.utils.PrefsManager
import tk.zwander.widgetdrawer.utils.canDrawOverlays
import tk.zwander.widgetdrawer.utils.vibrate
import tk.zwander.widgetdrawer.views.Drawer
import tk.zwander.widgetdrawer.views.Handle



@SuppressLint("InflateParams")
class DrawerService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val CHANNEL = "widget_drawer_main"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, DrawerService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DrawerService::class.java))
        }
    }

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val nm by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val appOpsManager by lazy { getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager }

    private val handle by lazy { Handle(this) }
    private val drawer by lazy { LayoutInflater.from(this)
        .inflate(R.layout.drawer_layout, null, false) as Drawer }
    private val prefs by lazy { PrefsManager.getInstance(this) }
    private val overlayListener = AppOpsManager.OnOpChangedListener { op, packageName ->
        if (packageName == this.packageName) {
            when(op) {
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW -> {
                    val allowed = appOpsManager.checkOpNoThrow(op, Process.myUid(), this.packageName) == AppOpsManager.MODE_ALLOWED
                    if (allowed) addHandle()
                    else {
                        stopForeground(true)
                        stopSelf()
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent) = null

    override fun onCreate() {
        drawer.onCreate()
        prefs.addPrefListener(this)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val channel = NotificationChannel(CHANNEL, resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
            channel.enableVibration(false)
            channel.enableLights(false)
            nm.createNotificationChannel(channel)
        }

        startForeground(100, NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle(resources.getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build())

        handle.onOpenListener = {
            if (!drawer.isAttachedToWindow) {
                vibrate(10)
                drawer.showDrawer()
                remHandle()
            }
        }

        drawer.hideListener = {
            addHandle()
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            appOpsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, packageName, overlayListener)

        if (canDrawOverlays) {
            addHandle()
        } else {
            requestPermission()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        when (newConfig?.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                remHandle()
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                addHandle()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefsManager.SHOW_HANDLE -> if (prefs.showHandle) addHandle() else remHandle()
        }
    }

    private fun addHandle() {
        if (prefs.showHandle) {
            try {
                windowManager.addView(handle, handle.params)
            } catch (e: Exception) {}
        }
    }

    private fun remHandle() {
        try {
            windowManager.removeView(handle)
        } catch (e: Exception) {}
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermission() {
        prefs.enabled = false
        Toast.makeText(this, R.string.allow_overlay, Toast.LENGTH_LONG).show()
        val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        myIntent.data = Uri.parse("package:$packageName")
        myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(myIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        remHandle()

        drawer.onDestroy()
        handle.onDestroy()
        prefs.removePrefListener(this)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            appOpsManager.stopWatchingMode(overlayListener)
    }
}
