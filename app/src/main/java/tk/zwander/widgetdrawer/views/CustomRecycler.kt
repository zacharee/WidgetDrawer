package tk.zwander.widgetdrawer.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class CustomRecycler : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val touchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            val drag = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END
            val swipe = ItemTouchHelper.START or ItemTouchHelper.END

            return makeMovementFlags(drag, swipe)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder
        ): Boolean {
            return allowReorder && onMoveListener?.invoke(recyclerView, viewHolder, target) == true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            if (allowReorder) onSwipeListener?.invoke(viewHolder, direction)
        }

    })


    var onMoveListener: ((recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder) -> Boolean)? = null

    var onSwipeListener: ((viewHolder: RecyclerView.ViewHolder,
                           direction: Int) -> Unit)? = null

    var allowReorder = false
        set(value) {
            touchHelper.attachToRecyclerView(if (value) this else null)
            field = value
        }

    override fun onFinishInflate() {
        super.onFinishInflate()

//        touchHelper.attachToRecyclerView(this)
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        val sup = super.onInterceptTouchEvent(e)
        return if (allowReorder) sup else false
    }
}