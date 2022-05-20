package tk.zwander.widgetdrawer.host

import android.appwidget.AppWidgetHost
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * An implementation of [AppWidgetHost] used on devices where the hidden API object
 * [RemoteViews.OnClickHandler] is an interface (i.e. Android 10 and above). Java makes
 * it pretty easy to implement interfaces through reflection, so no ByteBuddy needed here.
 */
@SuppressLint("PrivateApi")
class WidgetHostInterface(context: Context, id: Int)
    : WidgetHostCompat(
    context, id, Proxy.newProxyInstance(
        Class.forName("android.widget.RemoteViews\$OnClickHandler").classLoader,
        arrayOf(Class.forName("android.widget.RemoteViews\$OnClickHandler")),
        InnerOnClickHandlerQ(context)
    )
) {
    class InnerOnClickHandlerQ(context: Context) : BaseInnerOnClickHandler(context), InvocationHandler {
        @SuppressLint("BlockedPrivateApi", "PrivateApi")
        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>): Any {
            val view = args[0] as View
            val pi = args[1] as PendingIntent
            val response = args[2]

            val responseClass = Class.forName("android.widget.RemoteViews\$RemoteResponse")

            val getLaunchOptions = responseClass.getDeclaredMethod("getLaunchOptions", View::class.java)
            val startPendingIntent = RemoteViews::class.java.getDeclaredMethod(
                "startPendingIntent", View::class.java, PendingIntent::class.java, android.util.Pair::class.java)

            @Suppress("UNCHECKED_CAST")
            val launchOptions = getLaunchOptions.invoke(response, view) as android.util.Pair<Intent, ActivityOptions>

            checkPendingIntent(pi)

            return startPendingIntent.invoke(null, view, pi, launchOptions) as Boolean
        }
    }
}