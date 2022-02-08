package tk.zwander.widgetdrawer.misc

import android.annotation.SuppressLint
import android.content.Context
import tk.zwander.widgetdrawer.host.WidgetHostCompat
import tk.zwander.widgetdrawer.utils.PrefsManager
import kotlin.random.Random

class ShortcutIdManager private constructor(private val context: Context, private val host: WidgetHostCompat) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: ShortcutIdManager? = null

        fun getInstance(context: Context, host: WidgetHostCompat): ShortcutIdManager {
            if (instance == null) instance = ShortcutIdManager(context.applicationContext, host)
            return instance!!
        }
    }

    private val prefs by lazy { PrefsManager.getInstance(context) }

    @SuppressLint("NewApi")
    fun allocateShortcutId(): Int {
        val current = prefs.shortcutIds

        val random = Random(System.currentTimeMillis())
        var id = random.nextInt()

        //AppWidgetHost.appWidgetIds has existed since at least 5.1.1, just hidden
        while (current.contains(id.toString()) && host.appWidgetIds.contains(id))
            id = random.nextInt()

        prefs.addShortcutId(id.toString())

        return id
    }

    fun removeShortcutId(id: Int) {
        prefs.removeShortcutId(id.toString())
        prefs.widgetSizes = prefs.widgetSizes.apply { remove(id) }
    }
}