package tk.zwander.widgetdrawer.misc

import android.content.pm.ApplicationInfo
import android.os.Parcelable

data class WidgetInfo(
    var widgetName: String,
    var previewImg: Int,
    var component: Parcelable,
    var appInfo: ApplicationInfo
) : BaseInfo(), Comparable<WidgetInfo> {
    override fun compareTo(other: WidgetInfo) =
        widgetName.compareTo(other.widgetName)
}