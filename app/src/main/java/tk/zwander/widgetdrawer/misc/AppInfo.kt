package tk.zwander.widgetdrawer.misc

import android.graphics.drawable.Drawable
import java.util.*

data class AppInfo(
    var appName: String,
    var appIcon: Drawable,
    var widgets: TreeSet<WidgetInfo> = TreeSet()
) : BaseInfo(), Comparable<AppInfo> {
    override fun compareTo(other: AppInfo) =
            appName.compareTo(other.appName)
}