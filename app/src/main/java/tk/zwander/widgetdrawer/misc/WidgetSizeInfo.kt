package tk.zwander.widgetdrawer.misc

import android.content.Context
import tk.zwander.widgetdrawer.utils.prefs

data class WidgetSizeInfo(
    private var widthSpanSize: Int,
    private var heightSpanSize: Int,
    val id: Int
) {
    var safeWidthSpanSize: Int
        get() = widthSpanSize.coerceAtLeast(1)
        set(value) {
            widthSpanSize = value.coerceAtLeast(1)
        }

    var safeHeightSpanSize: Int
        get() = heightSpanSize.coerceAtLeast(1)
        set(value) {
            heightSpanSize = value.coerceAtLeast(1)
        }

    fun getSafeWidthSpanSize(context: Context): Int {
        return safeWidthSpanSize.coerceAtMost(context.prefs.columnCount)
    }
}