package tk.zwander.widgetdrawer.misc

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class ShortcutData(
    var label: String?,
    var icon: Bitmap?,
    var activityInfo: ActivityInfo?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Bitmap::class.java.classLoader),
        parcel.readParcelable(ActivityInfo::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeParcelable(icon, flags)
        parcel.writeParcelable(activityInfo, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShortcutData> {
        override fun createFromParcel(parcel: Parcel): ShortcutData {
            return ShortcutData(parcel)
        }

        override fun newArray(size: Int): Array<ShortcutData?> {
            return arrayOfNulls(size)
        }
    }
}