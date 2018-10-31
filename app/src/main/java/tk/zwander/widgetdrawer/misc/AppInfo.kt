package tk.zwander.widgetdrawer.misc

import android.graphics.drawable.Drawable

data class AppInfo(
    var appName: String,
    var appIcon: Drawable,
    var widgets: ArrayList<WidgetInfo> = ArrayList()
) : BaseInfo(), Comparable<AppInfo> {
    override fun compareTo(other: AppInfo) =
            appName.compareTo(other.appName)
}