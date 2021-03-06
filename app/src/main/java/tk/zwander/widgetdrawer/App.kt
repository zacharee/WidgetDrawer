package tk.zwander.widgetdrawer

import android.app.Application
import android.content.*
import android.os.Build
import android.view.LayoutInflater
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.services.EnhancedViewService
import tk.zwander.widgetdrawer.utils.PrefsManager
import tk.zwander.widgetdrawer.views.Drawer
import tk.zwander.widgetdrawer.views.Handle
import java.lang.reflect.Method

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    val prefs by lazy { PrefsManager.getInstance(this) }

    val drawer by lazy {
        LayoutInflater.from(this)
            .inflate(R.layout.drawer_layout, null, false) as Drawer
    }
    val handle by lazy { Handle(this) }
    val accessibilityListeners = ArrayList<(Boolean) -> Unit>()

    private val bc = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                EnhancedViewService.ACTION_ACCESSIBILITY_CONNECTED -> {
                    accessibilityListeners.forEach { it(true) }
                }
                EnhancedViewService.ACTION_ACCESSIBILITY_DISCONNECTED -> {
                    accessibilityListeners.forEach { it(false) }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (prefs.enabled)
            DrawerService.start(this@App)

        prefs.addPrefListener(this@App)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
            val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                "getDeclaredMethod", String::class.java, arrayOf<Class<*>>()::class.java)

            val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
            val getRuntime = getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
            val setHiddenApiExemptions = getDeclaredMethod.invoke(
                vmRuntimeClass, "setHiddenApiExemptions", arrayOf(arrayOf<String>()::class.java)) as Method

            val vmRuntime = getRuntime.invoke(null)

            setHiddenApiExemptions.invoke(vmRuntime, arrayOf("L"))
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(bc, IntentFilter().apply {
                addAction(EnhancedViewService.ACTION_ACCESSIBILITY_CONNECTED)
                addAction(EnhancedViewService.ACTION_ACCESSIBILITY_DISCONNECTED)
            })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefsManager.ENABLED -> {
                if (prefs.enabled) DrawerService.start(this)
                else DrawerService.stop(this)
            }
        }
    }
}