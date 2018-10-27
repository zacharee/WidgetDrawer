package tk.zwander.widgetdrawer.misc

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import tk.zwander.widgetdrawer.views.DrawerHostView

class DrawerHost(val context: Context, id: Int) : AppWidgetHost(context, id) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return DrawerHostView(context)
    }
}