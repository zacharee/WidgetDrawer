package tk.zwander.widgetdrawer.utils

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tk.zwander.widgetdrawer.misc.OverrideWidgetInfo

class PrefsManager(context: Context) {
    companion object {
        const val WIDGETS = "saved_widgets"
        const val ENABLED = "enabled"
        const val HANDLE_SIDE = "handle_side"
        const val HANDLE_Y = "handle_y"
        const val HANDLE_HEIGHT = "handle_height"
        const val HANDLE_COLOR = "handle_color"

        const val HANDLE_LEFT = 0
        const val HANDLE_RIGHT = 1
        const val HANDLE_Y_DEF = 64
        const val HANDLE_HEIGHT_DEF = 64
        const val HANDLE_COLOR_DEF = Color.WHITE
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var currentWidgets: List<OverrideWidgetInfo>
        get() {
            return Gson().fromJson<ArrayList<OverrideWidgetInfo>>(
                getString(WIDGETS, null) ?: return ArrayList(),
                object : TypeToken<ArrayList<OverrideWidgetInfo>>() {}.type
            )
        }
        set(value) {
            putString(WIDGETS, Gson().toJson(value))
        }
    var enabled: Boolean
        get() = getBoolean(ENABLED, false)
        set(value) {
            putBoolean(ENABLED, value)
        }
    var handleSide: Int
        get() = getInt(HANDLE_SIDE, HANDLE_RIGHT)
        set(value) {
            putInt(HANDLE_SIDE, value)
        }
    var handleYDp: Int
        get() = getInt(HANDLE_Y, HANDLE_Y_DEF)
        set(value) {
            putInt(HANDLE_Y, value)
        }
    var handleHeightDp: Int
        get() = getInt(HANDLE_HEIGHT, HANDLE_HEIGHT_DEF)
        set(value) {
            putInt(HANDLE_HEIGHT, value)
        }
    var handleColor: Int
        get() = getInt(HANDLE_COLOR, HANDLE_COLOR_DEF)
        set(value) {
            putInt(HANDLE_COLOR, value)
        }


    fun getString(key: String, def: String?) = prefs.getString(key, def)
    fun getFloat(key: String, def: Float) = prefs.getFloat(key, def)
    fun getInt(key: String, def: Int) = prefs.getInt(key, def)
    fun getLong(key: String, def: Long) = prefs.getLong(key, def)
    fun getBoolean(key: String, def: Boolean) = prefs.getBoolean(key, def)
    fun getStringSet(key: String, def: Set<String>) = prefs.getStringSet(key, def)

    fun putString(key: String, value: String) = prefs.edit().putString(key, value).commit()
    fun putFloat(key: String, value: Float) = prefs.edit().putFloat(key, value).commit()
    fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).commit()
    fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).commit()
    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).commit()
    fun putStringSet(key: String, value: Set<String>) = prefs.edit().putStringSet(key, value).commit()

    fun remove(key: String) = prefs.edit().remove(key).commit()
}