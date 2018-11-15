package tk.zwander.widgetdrawer.misc

import android.annotation.SuppressLint
import android.content.Context
import tk.zwander.widgetdrawer.utils.PrefsManager
import kotlin.random.Random

class ShortcutIdManager private constructor(private val context: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: ShortcutIdManager? = null

        fun getInstance(context: Context): ShortcutIdManager {
            if (instance == null) instance = ShortcutIdManager(context.applicationContext)
            return instance!!
        }
    }

    private val prefs by lazy { PrefsManager.getInstance(context) }

    fun allocateShortcutId(): Int {
        val current = prefs.shortcutIds

        val random = Random(System.currentTimeMillis())
        var id = random.nextInt()

        while (current.contains(id.toString())) id = random.nextInt()

        prefs.addShortcutId(id.toString())

        return id
    }

    fun removeShortcutId(id: Int) {
        prefs.removeShortcutId(id.toString())
    }
}