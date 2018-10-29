package tk.zwander.widgetdrawer.views

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup

class DrawerHostView(context: Context) : AppWidgetHostView(context) {
    private val recView: DrawerRecycler
        get() = parent.parent.parent as DrawerRecycler

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        layoutParams = layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (recView.allowReorder) {
            performClick()
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }
}