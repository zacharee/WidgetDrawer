package tk.zwander.widgetdrawer.misc

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import tk.zwander.widgetdrawer.adapters.DrawerAdapter

class BaseWidgetInfo(
    var type: Int,
    var label: String? = null,
    var iconBmp: Bitmap? = null,
    var activityInfo: ActivityInfo? = null,
    var id: Int,
    var forcedHeight: Int = DrawerAdapter.SIZE_DEF,
    var isFullWidth: Boolean = false,
    var shortcutIntent: Intent? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readParcelable(Bitmap::class.java.classLoader),
        parcel.readParcelable(ActivityInfo::class.java.classLoader),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(Intent::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeString(label)
        parcel.writeParcelable(iconBmp, flags)
        parcel.writeParcelable(activityInfo, flags)
        parcel.writeInt(id)
        parcel.writeInt(forcedHeight)
        parcel.writeByte(if (isFullWidth) 1 else 0)
        parcel.writeParcelable(shortcutIntent, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BaseWidgetInfo> {
        override fun createFromParcel(parcel: Parcel): BaseWidgetInfo {
            return BaseWidgetInfo(parcel)
        }

        override fun newArray(size: Int): Array<BaseWidgetInfo?> {
            return arrayOfNulls(size)
        }

        fun shortcut(
            label: String?,
            icon: Bitmap?,
            activityInfo: ActivityInfo?,
            id: Int,
            intent: Intent?
        ): BaseWidgetInfo {
            return BaseWidgetInfo(
                TYPE_SHORTCUT,
                label,
                icon,
                activityInfo,
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
                null,
                -1,
                DrawerAdapter.SIZE_DEF,
                false
            )

        const val TYPE_WIDGET = 0
        const val TYPE_SHORTCUT = 1
        const val TYPE_HEADER = 2
    }

}