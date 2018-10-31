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

class DrawerHostView(context: Context) : AppWidgetHostView(context), NestedScrollingChild {
    private val recView by lazy { parent.parent.parent as DrawerRecycler }
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            val newEvent = MotionEvent.obtain(e2)

            scaleMotionEvent(newEvent)

            recView.onTouchEvent(newEvent)

            newEvent.recycle()
            return true
        }
    })

    init {
        id = R.id.drawer_view
    }

    val hasListView by lazy { hasListView(this) }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        layoutParams = layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        enableNestedScrolling(this)
        isNestedScrollingEnabled = true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (recView.allowReorder) {
            performClick()
            true
        } else {
            onTouchEvent(ev)
        }
    }

    private var notList = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> notList = notAListView(event)
            MotionEvent.ACTION_UP -> notList = false
        }

        if (!hasListView || notList) {
            return gestureDetector.onTouchEvent(event)
        }
        return false
    }

    fun setPadding(paddingPx: Int) {
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

    private fun enableNestedScrolling(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            child.isNestedScrollingEnabled = true

            if (child is ViewGroup) enableNestedScrolling(child)
        }
    }

    private fun hasListView(parent: ViewGroup): Boolean {
        return if (parent is ListView) true
        else {
            var hasList = false
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child is ViewGroup) {
                    hasList = hasListView(child)
                    if (hasList) break
                }
            }
            hasList
        }
    }

    private fun findListView(parent: ViewGroup): ListView? {
        return if (parent is ListView) parent
        else {
            var potentialList: ListView? = null

            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child is ViewGroup) potentialList = findListView(child)
                if (potentialList != null) break
            }

            potentialList
        }
    }

    private fun notAListView(event: MotionEvent?): Boolean {
        if (event == null) return false
        else {
            val listView = findListView(this@DrawerHostView) ?: return true

            val touchRect = Rect().apply { listView.getHitRect(this) }
            if (!touchRect.contains(event.x.toInt(), event.y.toInt())) {
                return true
            }
        }

        return false
    }

    private fun scaleMotionEvent(event: MotionEvent) {
        event.setLocation(event.x + left + (parent as ViewGroup).left + (parent.parent as ViewGroup).left,
            event.y + top + (parent as ViewGroup).top + (parent.parent as ViewGroup).top)
    }
}