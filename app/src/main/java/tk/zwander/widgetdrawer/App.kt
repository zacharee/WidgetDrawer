package tk.zwander.widgetdrawer

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import tk.zwander.widgetdrawer.services.DrawerService

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        ContextCompat.startForegroundService(this, Intent(this, DrawerService::class.java))
    }
}