package tk.zwander.widgetdrawer

import android.app.Application
import android.content.*
import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.PrefsManager

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    val prefs by lazy { PrefsManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()

        if (prefs.enabled) {
            DrawerService.start(this@App)
        }

        prefs.addPrefListener(this@App)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
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