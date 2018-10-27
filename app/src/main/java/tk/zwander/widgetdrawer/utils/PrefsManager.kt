package tk.zwander.widgetdrawer.utils

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tk.zwander.widgetdrawer.misc.OverrideWidgetInfo

class PrefsManager(context: Context) {
    companion object {
        const val WIDGETS = "saved_widgets"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getString(key: String, def: String?) = prefs.getString(key, def)
    fun getFloat(key: String, def: Float) = prefs.getFloat(key, def)
    fun getInt(key: String, def: Int) = prefs.getInt(key, def)
    fun getLong(key: String, def: Long) = prefs.getLong(key, def)
    fun getBoolean(key: String, def: Boolean) = prefs.getBoolean(key, def)
    fun getStringSet(key: String, def: Set<String>) = prefs.getStringSet(key, def)

    fun getCurrentWidgets(): ArrayList<OverrideWidgetInfo> {
        return Gson().fromJson<ArrayList<OverrideWidgetInfo>>(
            getString(WIDGETS, null) ?: return ArrayList(),
            object : TypeToken<ArrayList<OverrideWidgetInfo>>() {}.type
        )
    }

    fun putString(key: String, value: String) = prefs.edit().putString(key, value).commit()
    fun putFloat(key: String, value: Float) = prefs.edit().putFloat(key, value).commit()
    fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).commit()
    fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).commit()
    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).commit()
    fun putStringSet(key: String, value: Set<String>) = prefs.edit().putStringSet(key, value).commit()

    fun remove(key: String) = prefs.edit().remove(key).commit()

    fun putCurrentWidgets(widgets: List<OverrideWidgetInfo>) =
            putString(WIDGETS, Gson().toJson(widgets))
}