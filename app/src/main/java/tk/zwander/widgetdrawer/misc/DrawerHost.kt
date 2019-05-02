package tk.zwander.widgetdrawer.misc

import android.app.ActivityOptions
import android.app.PendingIntent
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import tk.zwander.widgetdrawer.views.Drawer
import tk.zwander.widgetdrawer.views.DrawerHostView
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * The super constructor here is technically a hidden API, but for whatever reason,
 * it's the one part of the OnClickHandler framework that's both accessible and
 * not blacklisted.
 *
 * On Pie, the RemoteViews$OnClickHandler class is actually blacklisted, but
 * it seems like the API blacklist doesn't actually work on classes themselves.
 * So it's possible to extend the class without much issue, and even override the methods.
 *
 * On Q, RemoteViews$OnClickHandler has been changed to an interface. While it's still possible
 * to implement it with a proxy, it has one method that's passed a View, PendingIntent, and
 * RemoteViews$RemoteResponse.
 *
 * Unfortunately, the RemoteResponse is now what contains the Intent that's needed to carry
 * out the click event, and that field is blacklisted. Currently, there doesn't seem to be
 * a way to intercept a widget click on Q, without setting Settings$Global.hidden_api_policy
 * to 1 or 0
 *
 * Proxy.newProxyInstance(
 * RemoteViews.OnClickHandler::class.java.classLoader,
 * arrayOf(RemoteViews.OnClickHandler::class.java),
 * DrawerHostView.InnerOnClickHandlerQ(DrawerHostView.OnClickHandlerDelegate(drawer))
 * ) as RemoteViews.OnClickHandler
 */
class DrawerHost(val context: Context, id: Int, drawer: Drawer) : AppWidgetHost(
    context,
    id,
    if (RemoteViews.OnClickHandler::class.java.isInterface) null
    else InnerOnClickHandlerPie(OnClickHandlerDelegate(drawer)),
    Looper.getMainLooper()
) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return DrawerHostView(context)
    }

    class InnerOnClickHandlerPie(private val delegate: OnClickHandlerDelegate) : RemoteViews.OnClickHandler() {
        private var enterAnimationId: Int = 0

        override fun onClickHandler(
            view: View,
            pendingIntent: PendingIntent,
            fillInIntent: Intent
        ): Boolean {
            return onClickHandler(view, pendingIntent, fillInIntent, 0)
        }

        override fun onClickHandler(
            view: View,
            pendingIntent: PendingIntent,
            fillInIntent: Intent,
            windowingMode: Int
        ): Boolean {
            return delegate.handleClick(view, pendingIntent, fillInIntent, windowingMode, enterAnimationId)
        }

        override fun setEnterAnimationId(enterAnimationId: Int) {
            this.enterAnimationId = enterAnimationId
        }
    }

    class InnerOnClickHandlerQ(private val delegate: OnClickHandlerDelegate) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>): Any {
            val view = args[0] as View
            val pi = args[1] as PendingIntent
            val response = args[2]

            return delegate.handleClick(view, pi, response)
        }
    }

    class OnClickHandlerDelegate(private val drawer: Drawer) {
        fun handleClick(
            view: View,
            pendingIntent: PendingIntent,
            response: Any
        ): Boolean {
            val remoteResponseClass = Class.forName("android.widget.RemoteViews\$RemoteResponse")

            return false
        }

        fun handleClick(
            view: View,
            pendingIntent: PendingIntent,
            fillInIntent: Intent,
            windowingMode: Int,
            enterAnimationId: Int
        ): Boolean {
            if (pendingIntent.isActivity) {
                drawer.hideDrawer()
            }

            try {
                val context = view.context
                val opts = ActivityOptions.makeCustomAnimation(context, enterAnimationId, 0)

                if (windowingMode != 0) {
                    opts.launchWindowingMode = windowingMode
                }
                context.startIntentSender(
                    pendingIntent.intentSender, fillInIntent,
                    Intent.FLAG_ACTIVITY_NEW_TASK,
                    Intent.FLAG_ACTIVITY_NEW_TASK, 0, opts.toBundle()
                )
            } catch (e: IntentSender.SendIntentException) {
                return false
            } catch (e: Exception) {
                return false
            }

            return true
        }
    }
}