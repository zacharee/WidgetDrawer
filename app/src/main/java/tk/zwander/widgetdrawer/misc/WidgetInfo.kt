package tk.zwander.widgetdrawer.misc

import android.appwidget.AppWidgetProviderInfo
import android.graphics.drawable.Drawable

data class WidgetInfo(
    var widgetName: String,
    var previewImg: Drawable?,
    var component: AppWidgetProviderInfo
) : BaseInfo(), Comparable<WidgetInfo> {
    override fun compareTo(other: WidgetInfo) =
        widgetName.compareTo(other.widgetName)
}