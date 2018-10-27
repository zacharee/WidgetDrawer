package tk.zwander.widgetdrawer.views

import android.annotation.SuppressLint
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ListView

class CustomHostView(context: Context) : AppWidgetHostView(context) {
    private val hasListView by lazy { hasListView(this) }
    private val recView: CustomRecycler
        get() = parent.parent as CustomRecycler

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
}