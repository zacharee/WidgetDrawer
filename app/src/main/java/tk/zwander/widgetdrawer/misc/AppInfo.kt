package tk.zwander.widgetdrawer.misc

import android.content.pm.ApplicationInfo

data class AppInfo(
    var appName: String,
    var appInfo: ApplicationInfo,
    var widgets: ArrayList<WidgetInfo> = ArrayList()
) : BaseInfo(), Comparable<AppInfo> {
    override fun compareTo(other: AppInfo) =
            appName.compareTo(other.appName)
}