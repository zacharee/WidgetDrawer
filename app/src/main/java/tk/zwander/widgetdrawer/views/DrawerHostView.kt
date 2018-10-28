package tk.zwander.widgetdrawer.views

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup

class DrawerHostView(context: Context) : AppWidgetHostView(context) {
    var selectionListener: ((id: Int) -> Unit)? = null

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
            selectionListener?.invoke(appWidgetId)
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }
}