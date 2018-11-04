package tk.zwander.widgetdrawer.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import tk.zwander.widgetdrawer.R

class CustomCard : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        background = resources.getDrawable(R.drawable.card_background)
    }

    fun setPadding(paddingPx: Int) {
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

    fun setCardBackgroundColor(color: Int) {
        (background as GradientDrawable).apply {
            setColor(color)
        }
    }
}