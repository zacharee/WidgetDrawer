package tk.zwander.widgetdrawer

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.PrefsManager

class App : Application() {
    val prefs by lazy { PrefsManager(this) }

    override fun onCreate() {
        super.onCreate()

        if (prefs.enabled)
            ContextCompat.startForegroundService(this, Intent(this, DrawerService::class.java))
    }
}