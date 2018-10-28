package tk.zwander.widgetdrawer.services

import android.annotation.TargetApi
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import tk.zwander.widgetdrawer.utils.PrefsManager

@TargetApi(Build.VERSION_CODES.N)
class DrawerToggleTile : TileService() {
    private val prefs by lazy { PrefsManager(this) }

    override fun onClick() {
        val newState = !prefs.enabled
        prefs.enabled = newState
        setState(newState)
    }

    private fun setState(enabled: Boolean) {
        qsTile?.apply {
            state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }

        if (enabled) DrawerService.start(this)
        else DrawerService.stop(this)
    }
}
