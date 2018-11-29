package tk.zwander.widgetdrawer.misc

import android.graphics.Bitmap
import android.os.Parcelable

data class WidgetInfo(
    var widgetName: String,
    var previewImg: Bitmap?,
    var component: Parcelable
) : BaseInfo(), Comparable<WidgetInfo> {
    override fun compareTo(other: WidgetInfo) =
        widgetName.compareTo(other.widgetName)
}