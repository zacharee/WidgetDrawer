package tk.zwander.widgetdrawer.misc

import android.graphics.drawable.Drawable
import android.os.Parcelable

data class WidgetInfo(
    var widgetName: String,
    var previewImg: Drawable?,
    var component: Parcelable
) : BaseInfo(), Comparable<WidgetInfo> {
    override fun compareTo(other: WidgetInfo) =
        widgetName.compareTo(other.widgetName)
}