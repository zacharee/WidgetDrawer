package tk.zwander.widgetdrawer.misc

import android.content.pm.ActivityInfo
import android.os.Parcel
import android.os.Parcelable
import tk.zwander.widgetdrawer.adapters.DrawerAdapter

class BaseWidgetInfo(
    var type: Int,
    var label: String? = null,
    var icon: Int = -1,
    var activityInfo: ActivityInfo? = null,
    var id: Int,
    var forcedHeight: Int = DrawerAdapter.SIZE_DEF,
    var isFullWidth: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readParcelable(ActivityInfo::class.java.classLoader),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeString(label)
        parcel.writeInt(icon)
        parcel.writeParcelable(activityInfo, flags)
        parcel.writeInt(id)
        parcel.writeInt(forcedHeight)
        parcel.writeByte(if (isFullWidth) 1 else 0)
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
            icon: Int,
            activityInfo: ActivityInfo?,
            id: Int
        ): BaseWidgetInfo {
            return BaseWidgetInfo(
                TYPE_SHORTCUT,
                label,
                icon,
                activityInfo,
                id
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
                -1,
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
                -1,
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