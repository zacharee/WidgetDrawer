package tk.zwander.widgetdrawer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.views.Drawer
import tk.zwander.widgetdrawer.views.Handle

class DrawerService : Service() {
    companion object {
        private const val CHANNEL = "widget_drawer_main"
    }

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val nm by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private val handle by lazy { Handle(this) }
    private val drawer by lazy { LayoutInflater.from(this).inflate(R.layout.drawer_layout, null, false) as Drawer }

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

        windowManager.addView(handle, handle.params)
        handle.onOpenListener = {
            if (!drawer.isAttachedToWindow) {
                drawer.showDrawer()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        drawer.onDestroy()
    }
}
