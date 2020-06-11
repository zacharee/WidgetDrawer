package tk.zwander.widgetdrawer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.preference.PreferenceManager
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import tk.zwander.helperlib.dpAsPx
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.BaseWidgetInfo
import tk.zwander.widgetdrawer.misc.WidgetInfo
import tk.zwander.widgetdrawer.misc.WidgetSizeInfo

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
        const val COLUMN_COUNT = "column_count"
        const val WIDGET_SIZE_INFO = "widget_size_info"

        @SuppressLint("RtlHardcoded")
        const val HANDLE_LEFT = Gravity.LEFT
        @SuppressLint("RtlHardcoded")
        const val HANDLE_RIGHT = Gravity.RIGHT
        const val HANDLE_UNCHANGED = -1
        const val HANDLE_COLOR_DEF = Color.WHITE

        const val DRAWER_BACKGROUND_COLOR = "drawer_background_color"

        @SuppressLint("StaticFieldLeak")
        private var instance: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            if (instance == null) instance = PrefsManager(context.applicationContext)
            return instance!!
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val gson = GsonBuilder()
        .create()

    var currentWidgets: List<BaseWidgetInfo>
        get() {
            return GsonBuilder()
                .setExclusionStrategies(CrashFixExclusionStrategy())
                .registerTypeAdapter(Uri::class.java, GsonUriHandler())
                .registerTypeAdapter(Intent::class.java, GsonIntentHandler())
                .create()
                .fromJson<ArrayList<BaseWidgetInfo>>(
                    getString(WIDGETS, null) ?: return ArrayList(),
                    object : TypeToken<ArrayList<BaseWidgetInfo>>() {}.type
                ).apply { removeAll { it.id == -1 } }
        }
        set(value) {
            putString(
                WIDGETS, GsonBuilder()
                    .setExclusionStrategies(CrashFixExclusionStrategy())
                    .registerTypeAdapter(Uri::class.java, GsonUriHandler())
                    .registerTypeAdapter(Intent::class.java, GsonIntentHandler())
                    .create()
                    .toJson(ArrayList(value)
                        .apply { removeAll { it.id == -1 } })
            )
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
        get() = getFloat(HANDLE_Y, context.dpAsPx(64).toFloat())
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
        get() = HashSet(getStringSet(CURRENT_SHORTCUT_IDS, HashSet())!!)
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
    var drawerBg: Int
        get() = getInt(DRAWER_BACKGROUND_COLOR, ContextCompat.getColor(context, R.color.drawerBackgroundDefault))
        set(value) {
            putInt(DRAWER_BACKGROUND_COLOR, value)
        }
    var columnCount: Int
        get() = getInt(COLUMN_COUNT, 2)
        set(value) {
            putInt(COLUMN_COUNT, value)
        }
    var widgetSizes: HashMap<Int, WidgetSizeInfo>
        get() = gson.fromJson(
            getString(WIDGET_SIZE_INFO, null),
            object : TypeToken<HashMap<Int, WidgetSizeInfo>>() {}.type
        ) ?: HashMap()
        set(value) {
            putString(
                WIDGET_SIZE_INFO,
                gson.toJson(value)
            )
        }

    fun updateWidgetSize(id: Int, newWidth: Int, newHeight: Int) {
        val sizeInfo = WidgetSizeInfo(newWidth, newHeight, id)

        updateWidgetSize(sizeInfo)
    }

    fun updateWidgetSize(widgetSizeInfo: WidgetSizeInfo) {
        widgetSizes = widgetSizes.apply { this[widgetSizeInfo.id] = widgetSizeInfo }
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