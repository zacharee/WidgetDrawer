package tk.zwander.widgetdrawer.utils

import android.annotation.SuppressLint
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.WindowManager
import tk.zwander.widgetdrawer.misc.ShortcutData

class EventManager private constructor(private val context: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var _instance: EventManager? = null

        fun getInstance(context: Context): EventManager {
            return _instance ?: EventManager(context.applicationContext ?: context).apply {
                _instance = this
            }
        }
    }

    private val listeners: MutableList<ListenerInfo<Event>> = ArrayList()
    private val observers: MutableList<EventObserver> = ArrayList()

    inline fun <reified T : Event> addListener(noinline listener: (T) -> Unit) {
        addListener(
            ListenerInfo(
                T::class.java,
                listener
            )
        )
    }

    fun <T : Event> addListener(listenerInfo: ListenerInfo<T>) {
        listeners.add(listenerInfo as ListenerInfo<Event>)
    }

    fun addObserver(observer: EventObserver) {
        observers.add(observer)
    }

    inline fun <reified T : Event> removeListener(noinline listener: (T) -> Unit) {
        removeListener(
            ListenerInfo(
                T::class.java,
                listener
            )
        )
    }

    fun <T : Event> removeListener(listenerInfo: ListenerInfo<T>) {
        listeners.remove(listenerInfo as ListenerInfo<Event>)
    }

    fun removeObserver(observer: EventObserver) {
        observers.remove(observer)
    }

    fun sendEvent(event: Event) {
        observers.forEach {
            it.onEvent(event)
        }

        listeners.filter { it.listenerClass == event::class.java }
            .forEach {
                it.listener.invoke(event)
            }
    }
}

sealed class Event {
    data class PermissionResult(val success: Boolean, val widgetId: Int) : Event()
    data class WidgetConfigResult(val success: Boolean, val widgetId: Int) : Event()
    data class ShortcutConfigResult(val success: Boolean, val data: ShortcutData?, val intent: Intent?, val name: String?, val iconRes: Intent.ShortcutIconResource?, val iconBmp: Bitmap?) : Event()
    data class PickWidgetResult(val success: Boolean, val providerInfo: AppWidgetProviderInfo) : Event()
    data class PickShortcutResult(val success: Boolean, val shortcutData: ShortcutData) : Event()

    data class ShowDrawer(val wm: WindowManager? = null, val type: Int = getProperWLPType()) : Event()
    data class ShowHandle(val wm: WindowManager? = null, val type: Int = getProperWLPType()) : Event()

    object PickFailedResult : Event()
    object OpenDrawerFromAccessibility : Event()
    object AddHandleFromAccessibility : Event()
    object AccessibilityConnected : Event()
    object AccessibilityDisconnected : Event()
    object CloseDrawer : Event()
}

interface EventObserver {
    fun onEvent(event: Event)
}

data class ListenerInfo<T : Event>(
    val listenerClass: Class<T>,
    val listener: (T) -> Unit
)