package tk.zwander.widgetdrawer.misc

import android.content.Intent
import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import tk.zwander.widgetdrawer.adapters.DrawerAdapter

@Parcelize
data class BaseWidgetInfo(
    var type: Int,
    var label: String? = null,
    var iconBmp: Bitmap? = null,
    var id: Int,
    var forcedHeight: Int = DrawerAdapter.SIZE_DEF,
    var isFullWidth: Boolean = false,
    var shortcutIntent: Intent? = null
) : Parcelable {
    companion object {
        fun shortcut(
            label: String?,
            icon: Bitmap?,
            id: Int,
            intent: Intent?
        ): BaseWidgetInfo {
            return BaseWidgetInfo(
                TYPE_SHORTCUT,
                label,
                icon,
                id,
                DrawerAdapter.SIZE_DEF,
                false,
                intent
            )
        }

        fun widget(
            id: Int,
            forcedHeight: Int = DrawerAdapter.SIZE_DEF,
            isFullWidth: Boolean = false
        ): BaseWidgetInfo {
            return BaseWidgetInfo(
                TYPE_WIDGET,
                null,
                null,
                id,
                forcedHeight,
                isFullWidth
            )
        }

        fun header() =
            BaseWidgetInfo(
                TYPE_HEADER,
                null,
                null,
                -1,
                DrawerAdapter.SIZE_DEF,
                false
            )

        const val TYPE_WIDGET = 0
        const val TYPE_SHORTCUT = 1
        const val TYPE_HEADER = 2
    }

    override fun describeContents(): Int {
        return 0
    }
}