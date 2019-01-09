package tk.zwander.widgetdrawer.misc

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShortcutData(
    var label: String?,
    var icon: Bitmap?,
    var activityInfo: ActivityInfo?
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }
}