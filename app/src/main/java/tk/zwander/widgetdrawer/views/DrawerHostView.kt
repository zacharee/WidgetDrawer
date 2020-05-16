package tk.zwander.widgetdrawer.views

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.view.NestedScrollingChild
import tk.zwander.widgetdrawer.R

@SuppressLint("ViewConstructor")
class DrawerHostView(context: Context) : AppWidgetHostView(context), NestedScrollingChild {
    private val recView by lazy { parent.parent.parent as DrawerRecycler }

    init {
        id = R.id.drawer_view
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        layoutParams = layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        setPadding(0, 0, 0, 0)
        setPaddingRelative(0, 0, 0, 0)

        enableNestedScrolling(this)
        isNestedScrollingEnabled = true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (recView.allowReorder) {
            callOnClick()
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    private fun enableNestedScrolling(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            child.isNestedScrollingEnabled = true

            if (child is ViewGroup) enableNestedScrolling(child)
        }
    }
}