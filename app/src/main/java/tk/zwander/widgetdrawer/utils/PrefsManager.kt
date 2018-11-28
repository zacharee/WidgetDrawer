package tk.zwander.widgetdrawer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager
import android.view.Gravity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import tk.zwander.widgetdrawer.misc.BaseWidgetInfo

class PrefsManager private constructor(private val context: Context) {
    companion object {
        const val WIDGETS = "saved_widgets"
        const val ENABLED = "enabled"
        const val HANDLE_SIDE = "handle_side"
        const val HANDLE_Y = "handle_y"
        const val HANDLE_HEIGHT = "handle_height"
        const val HANDLE_WIDTH = "handle_width"
        const val HANDLE_COLOR = "handle_color"
        const val HANDLE_SHADOW = "handle_shadow"
        const val TRANSPARENT_WIDGETS = "transparent_widgets"
        const val CURRENT_SHORTCUT_IDS = "shortcut_ids"
        const val SHOW_HANDLE = "show_handle"
        const val CLOSE_ON_EMPTY_TAP = "close_on_empty_tap"

        @SuppressLint("RtlHardcoded")
        const val HANDLE_LEFT = Gravity.LEFT
        @SuppressLint("RtlHardcoded")
        const val HANDLE_RIGHT = Gravity.RIGHT
        const val HANDLE_UNCHANGED = -1
        const val HANDLE_COLOR_DEF = Color.WHITE

        @SuppressLint("StaticFieldLeak")
        private var instance: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            if (instance == null) instance = PrefsManager(context.applicationContext)
            return instance!!
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var currentWidgets: List<BaseWidgetInfo>
        get() {
            return GsonBuilder().apply { addDeserializationExclusionStrategy(SuperclassExclusionStrategy()) }
                .create().fromJson<ArrayList<BaseWidgetInfo>>(
                getString(WIDGETS, null) ?: return ArrayList(),
                object : TypeToken<ArrayList<BaseWidgetInfo>>() {}.type
            ).apply { removeAll { it.id == -1 } }
        }
        set(value) {
            putString(WIDGETS, GsonBuilder().apply { addSerializationExclusionStrategy(SuperclassExclusionStrategy()) }
                .create().toJson(ArrayList(value).apply { removeAll { it.id == -1 } }))
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
    var handleYPx: Float
        get() = getFloat(HANDLE_Y, context.pxAsDp(64))
        set(value) {
            putFloat(HANDLE_Y, value)
        }
    var handleHeightDp: Int
        get() = getInt(HANDLE_HEIGHT, 140)
        set(value) {
            putInt(HANDLE_HEIGHT, value)
        }
    var handleWidthDp: Int
        get() = getInt(HANDLE_WIDTH, 6)
        set(value) {
            putInt(HANDLE_HEIGHT, value)
        }
    var handleColor: Int
        get() = getInt(HANDLE_COLOR, HANDLE_COLOR_DEF)
        set(value) {
            putInt(HANDLE_COLOR, value)
        }
    var handleShadow: Boolean
        get() = getBoolean(HANDLE_SHADOW, true)
        set(value) {
            putBoolean(HANDLE_SHADOW, value)
        }
    var transparentWidgets: Boolean
        get() = getBoolean(TRANSPARENT_WIDGETS, false)
        set(value) {
            putBoolean(TRANSPARENT_WIDGETS, value)
        }
    var shortcutIds: Set<String>
        get() = HashSet(getStringSet(CURRENT_SHORTCUT_IDS, HashSet()))
        set(value) {
            putStringSet(CURRENT_SHORTCUT_IDS, value.toSet())
        }
    var showHandle: Boolean
        get() = getBoolean(SHOW_HANDLE, true)
        set(value) {
            putBoolean(SHOW_HANDLE, value)
        }
    var closeOnEmptyTap: Boolean
        get() = getBoolean(CLOSE_ON_EMPTY_TAP, false)
        set(value) {
            putBoolean(CLOSE_ON_EMPTY_TAP, value)
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

    fun addPrefListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
            prefs.registerOnSharedPreferenceChangeListener(listener)

    fun removePrefListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
            prefs.unregisterOnSharedPreferenceChangeListener(listener)

    fun addShortcutId(id: String) {
        shortcutIds = ArrayList(shortcutIds).apply { add(id) }.toSet()
    }

    fun removeShortcutId(id: String) {
        shortcutIds = ArrayList(shortcutIds).apply { remove(id) }.toSet()
    }
}