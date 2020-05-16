package tk.zwander.widgetdrawer.services

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.utils.*


@SuppressLint("InflateParams")
class DrawerService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val ACTION_OPEN_DRAWER = "open_drawer"
        const val ACTION_CLOSE_DRAWER = "close_drawer"

        private const val CHANNEL = "widget_drawer_main"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, DrawerService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DrawerService::class.java))
        }

        fun openDrawer(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_OPEN_DRAWER))
        }

        fun closeDrawer(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_CLOSE_DRAWER))
        }
    }

    private val nm by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val appOpsManager by lazy { getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager }

    private val handle by lazy { app.handle }
    private val drawer by lazy { app.drawer }
    private val overlayListener = AppOpsManager.OnOpChangedListener { op, packageName ->
        if (packageName == this.packageName) {
            when (op) {
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW -> {
                    val allowed = appOpsManager.checkOpNoThrow(
                        op,
                        Process.myUid(),
                        this.packageName
                    ) == AppOpsManager.MODE_ALLOWED
                    if (allowed) addHandle()
                    else {
                        stopForeground(true)
                        stopSelf()
                    }
                }
            }
        }
    }
    private val openReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_OPEN_DRAWER -> {
                    openDrawer()
                }

                ACTION_CLOSE_DRAWER -> {
                    closeDrawer()
                }
            }
        }
    }
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                }
            }

            if (canShowHandle()) {
                addHandle()
            } else {
                remHandle()
            }
        }
    }

    private var isScreenOn = false

    override fun onBind(intent: Intent) = null

    override fun onCreate() {
        drawer.onCreate()
        isScreenOn = (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
        prefs.addPrefListener(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(openReceiver, IntentFilter(ACTION_OPEN_DRAWER).apply {
            addAction(ACTION_CLOSE_DRAWER)
        })
        registerReceiver(screenStateReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
        app.accessibilityListeners.add {
            if (drawer.isAttachedToWindow) {
                closeDrawer()
                openDrawer()
            }

            if (handle.isAttachedToWindow) {
                remHandle()
                addHandle()
            }
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            val channel =
                NotificationChannel(CHANNEL, resources.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
            channel.enableVibration(false)
            channel.enableLights(false)
            nm.createNotificationChannel(channel)
        }

        startForeground(
            100, NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle(resources.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        )

        handle.onOpenListener = {
            if (!drawer.isAttachedToWindow) {
                vibrate(10)
                openDrawer()
            }
        }

        drawer.hideListener = {
            addHandle()
        }

        drawer.showListener = {
            remHandle()
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
            PrefsManager.SHOW_HANDLE -> if (canShowHandle()) addHandle() else remHandle()
        }
    }

    private fun addHandle() {
        if (canShowHandle()) {
            if (accessibilityConnected) EnhancedViewService.addHandle(this)
            else handle.show(overrideType = getProperWLPType())
        }
    }

    private fun remHandle() {
        handle.hide()
    }

    private fun openDrawer() {
        remHandle()
        if (accessibilityConnected) EnhancedViewService.addDrawer(this)
        else drawer.showDrawer(overrideType = getProperWLPType())
    }

    private fun closeDrawer() {
        drawer.hideDrawer()
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(openReceiver)
        unregisterReceiver(screenStateReceiver)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            appOpsManager.stopWatchingMode(overlayListener)
    }

    private fun canShowHandle(): Boolean =
        isScreenOn
                && prefs.showHandle
}
