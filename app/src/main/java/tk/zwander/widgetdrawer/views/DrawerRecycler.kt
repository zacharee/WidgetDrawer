package tk.zwander.widgetdrawer.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import tk.zwander.widgetdrawer.adapters.DrawerAdapter
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.PrefsManager

class DrawerRecycler : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return if (PrefsManager.getInstance(context).closeOnEmptyTap && !allowReorder) {
                DrawerService.closeDrawer(context)
                true
            } else false
        }
    })

    private val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
        ItemTouchHelper.START or ItemTouchHelper.END
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder
        ): Boolean {
            return allowReorder
                    && onMoveListener?.invoke(recyclerView, viewHolder, target) == true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            if (allowReorder) onSwipeListener?.invoke(viewHolder, direction)
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            return if (viewHolder !is DrawerAdapter.HeaderVH) super.getMovementFlags(recyclerView, viewHolder)
            else 0
        }

        override fun isItemViewSwipeEnabled() = allowReorder
        override fun isLongPressDragEnabled() = allowReorder
    })


    var onMoveListener: ((
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) -> Boolean)? = null

    var onSwipeListener: ((
        viewHolder: RecyclerView.ViewHolder,
        direction: Int
    ) -> Unit)? = null

    var allowReorder = false
        set(value) {
            (adapter as DrawerAdapter).isEditing = value
            field = value
        }

    override fun onFinishInflate() {
        super.onFinishInflate()

        touchHelper.attachToRecyclerView(this)
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val sup = super.onInterceptTouchEvent(e)
        return if (allowReorder) sup else false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return super.onTouchEvent(e) or gestureDetector.onTouchEvent(e)
    }
}