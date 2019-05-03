package tk.zwander.widgetdrawer

import android.app.Application
import android.content.SharedPreferences
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.PrefsManager
import java.lang.reflect.Method

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    val prefs by lazy { PrefsManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()

        if (prefs.enabled)
            DrawerService.start(this@App)

        prefs.addPrefListener(this@App)

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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefsManager.ENABLED -> {
                if (prefs.enabled) DrawerService.start(this)
                else DrawerService.stop(this)
            }
        }
    }
}