package tk.zwander.widgetdrawer.misc

import android.content.Intent
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseWidgetInfo(
    var type: Int,
    var label: String? = null,
    var iconBmpEncoded: String? = null,
    var iconRes: Intent.ShortcutIconResource? = null,
    var id: Int,
    var shortcutIntent: Intent? = null
) : Parcelable {
    companion object {
        fun shortcut(
            label: String?,
            icon: String?,
            iconRes: Intent.ShortcutIconResource?,
            id: Int,
            intent: Intent?
        ): BaseWidgetInfo {
            return BaseWidgetInfo(
                TYPE_SHORTCUT,
                label,
                icon,
                iconRes,
                id,
                intent
            )
        }

        fun widget(
            id: Int
        ): BaseWidgetInfo {
            return BaseWidgetInfo(
                TYPE_WIDGET,
                null,
                null,
                null,
                id
            )
        }

        fun header() =
            BaseWidgetInfo(
                TYPE_HEADER,
                null,
                null,
                null,
                -1
            )

        const val TYPE_WIDGET = 0
        const val TYPE_SHORTCUT = 1
        const val TYPE_HEADER = 2
    }

    override fun describeContents(): Int {
        return 0
    }
}
