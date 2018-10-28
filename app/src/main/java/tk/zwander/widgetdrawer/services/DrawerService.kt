package tk.zwander.widgetdrawer.services

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.utils.canDrawOverlays
import tk.zwander.widgetdrawer.views.Drawer
import tk.zwander.widgetdrawer.views.Handle

@SuppressLint("InflateParams")
class DrawerService : Service() {
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
                drawer.showDrawer()
            }
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            appOpsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, packageName, overlayListener)

        if (canDrawOverlays) {
            addHandle()
        } else {
            Toast.makeText(this, R.string.allow_overlay, Toast.LENGTH_LONG).show()
            requestPermission()
        }
    }

    private fun addHandle() {
        try {
            windowManager.addView(handle, handle.params)
        } catch (e: Exception) {}
    }

    private fun requestPermission() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        drawer.onDestroy()
        handle.onDestroy()

        try {
            windowManager.removeView(handle)
        } catch (e: Exception) {}

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            appOpsManager.stopWatchingMode(overlayListener)
    }
}
