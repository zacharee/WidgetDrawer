package tk.zwander.widgetdrawer.host

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetHost
import android.content.Context
import android.widget.RemoteViews
import tk.zwander.widgetdrawer.utils.prefs
import tk.zwander.widgetdrawer.views.Drawer

/**
 * Base widget host class. [WidgetHostClass], [WidgetHostInterface], and [WidgetHost12] extend this class and
 * are used conditionally, depending on whether [RemoteViews.OnClickHandler] is a class or interface
 * or [RemoteViews.InteractionHandler] is used on the device.
 *
 * @param context a Context object
 * @param id the ID of this widget host
 * @param onClickHandler the [RemoteViews.OnClickHandler] or [RemoteViews.InteractionHandler]
 * implementation defined in the subclass
 */
abstract class WidgetHostCompat(
    val context: Context,
    id: Int,
    onClickHandler: Any
) : AppWidgetHost(context, id) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: WidgetHostCompat? = null

        @SuppressLint("PrivateApi")
        fun getInstance(context: Context, id: Int, drawer: Drawer): WidgetHostCompat {
            return instance ?: run {
                if (!onClickHandlerExists) {
                    WidgetHost12(context.applicationContext ?: context, id, drawer)
                } else {
                    (if (Class.forName("android.widget.RemoteViews\$OnClickHandler").isInterface) {
                        WidgetHostInterface(context.applicationContext ?: context, id, drawer)
                    } else {
                        WidgetHostClass(context.applicationContext ?: context, id, drawer)
                    }).also {
                        instance = it
                    }
                }
            }
        }

        private val onClickHandlerExists: Boolean
            @SuppressLint("PrivateApi")
            get() = try {
                Class.forName("android.widget.RemoteViews\$OnClickHandler")
                true
            } catch (e: ClassNotFoundException) {
                //Should crash if neither exists
                Class.forName("android.widget.RemoteViews\$InteractionHandler")
                false
            }
    }

    init {
        AppWidgetHost::class.java
            .getDeclaredField(if (!onClickHandlerExists) "mInteractionHandler" else "mOnClickHandler")
            .apply {
                isAccessible = true
                set(this@WidgetHostCompat, onClickHandler)
            }
    }

    open class BaseInnerOnClickHandler(internal val context: Context, private val drawer: Drawer) {
        @SuppressLint("NewApi")
        fun checkPendingIntent(pendingIntent: PendingIntent) {
            if (pendingIntent.isActivity) {
                drawer.hideDrawer()
            }
        }
    }

    override fun deleteAppWidgetId(appWidgetId: Int) {
        super.deleteAppWidgetId(appWidgetId)

        context.prefs.apply {
            widgetSizes = widgetSizes.apply { remove(appWidgetId) }
        }
    }
}