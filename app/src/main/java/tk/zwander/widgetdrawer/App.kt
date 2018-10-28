package tk.zwander.widgetdrawer

import android.app.Application
import android.content.SharedPreferences
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.PrefsManager

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    val prefs by lazy { PrefsManager(this) }

    override fun onCreate() {
        super.onCreate()

        if (prefs.enabled)
            DrawerService.start(this)

        prefs.addPrefListener(this)
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