package tk.zwander.widgetdrawer.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import tk.zwander.widgetdrawer.utils.accessibilityConnected
import tk.zwander.widgetdrawer.utils.app

class EnhancedViewService : AccessibilityService() {
    companion object {
        const val ACTION_OPEN_DRAWER = "OPEN_DRAWER"
        const val ACTION_ADD_HANDLE = "ADD_HANDLE"

        const val ACTION_ACCESSIBILITY_CONNECTED = "ACCESSIBILITY_CONNECTED"
        const val ACTION_ACCESSIBILITY_DISCONNECTED = "ACCESSIBILITY_DISCONNECTED"

        fun addDrawer(context: Context) {
            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent(ACTION_OPEN_DRAWER))
        }

        fun addHandle(context: Context) {
            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent(ACTION_ADD_HANDLE))
        }
    }

    private val wm by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val bc = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_OPEN_DRAWER -> addDrawer()
                ACTION_ADD_HANDLE -> addHandle()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        accessibilityConnected = true

        LocalBroadcastManager.getInstance(this)
            .apply {
                registerReceiver(
                    bc,
                    IntentFilter().apply {
                        addAction(ACTION_ADD_HANDLE)
                        addAction(ACTION_OPEN_DRAWER)
                    }
                )
                sendBroadcast(Intent(ACTION_ACCESSIBILITY_CONNECTED))
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this)
            .apply {
                unregisterReceiver(bc)
                sendBroadcast(Intent(ACTION_ACCESSIBILITY_DISCONNECTED))
            }
    }

    override fun onInterrupt() {}
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    fun addDrawer() {
        app.drawer.showDrawer(wm, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
    }

    fun addHandle() {
        app.handle.show(wm, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
    }
}