package tk.zwander.widgetdrawer.services

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import tk.zwander.widgetdrawer.utils.PrefsManager

@TargetApi(Build.VERSION_CODES.N)
class DrawerToggleTile : TileService(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val prefs by lazy { PrefsManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        prefs.addPrefListener(this)
    }

    override fun onStartListening() {
        setState(prefs.enabled)
    }

    override fun onClick() {
        val newState = !prefs.enabled
        prefs.enabled = newState
        setState(newState)

        if (newState) DrawerService.start(this, PrefsManager.getInstance(this).openDrawerWithTile)
        else DrawerService.stop(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefsManager.ENABLED -> setState(prefs.enabled)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.removePrefListener(this)
    }

    private fun setState(enabled: Boolean) {
        qsTile?.apply {
            state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
}
