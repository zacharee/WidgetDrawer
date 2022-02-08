package tk.zwander.widgetdrawer.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.NestedScrollingParent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import tk.zwander.widgetdrawer.adapters.DrawerAdapter
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.PrefsManager

//Nested scrolling implementation from https://medium.com/widgetlabs-engineering/scrollable-nestedscrollviews-inside-recyclerview-ca65050d828a
class DrawerRecycler : RecyclerView, NestedScrollingParent {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        isNestedScrollingEnabled = true
    }

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
            return allowReorder && onMoveListener?.invoke(recyclerView, viewHolder, target) == true
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
        viewHolder: ViewHolder,
        target: ViewHolder
    ) -> Boolean)? = null

    var onSwipeListener: ((
        viewHolder: ViewHolder,
        direction: Int
    ) -> Unit)? = null

    var allowReorder = false
        set(value) {
            (adapter as DrawerAdapter).isEditing = value
            field = value
        }

    private var nestedScrollTarget: View? = null
    private var nestedScrollTargetIsBeingDragged = false
    private var nestedScrollTargetWasUnableToScroll = false
    private var skipsTouchInterception = false

    override fun onFinishInflate() {
        super.onFinishInflate()

        touchHelper.attachToRecyclerView(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val temporarilySkipsInterception = nestedScrollTarget != null
        if (temporarilySkipsInterception) {
            // If a descendant view is scrolling we set a flag to temporarily skip our onInterceptTouchEvent implementation
            skipsTouchInterception = true
        }

        // First dispatch, potentially skipping our onInterceptTouchEvent
        var handled = super.dispatchTouchEvent(ev)

        if (temporarilySkipsInterception) {
            skipsTouchInterception = false

            // If the first dispatch yielded no result or we noticed that the descendant view is unable to scroll in the
            // direction the user is scrolling, we dispatch once more but without skipping our onInterceptTouchEvent.
            // Note that RecyclerView automatically cancels active touches of all its descendants once it starts scrolling
            // so we don't have to do that.
            if (!handled || nestedScrollTargetWasUnableToScroll) {
                handled = super.dispatchTouchEvent(ev)
            }
        }

        return handled
    }

    override fun onInterceptTouchEvent(e: MotionEvent) =
        !skipsTouchInterception && super.onInterceptTouchEvent(e)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return super.onTouchEvent(e) or gestureDetector.onTouchEvent(e)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        if (target === nestedScrollTarget && !nestedScrollTargetIsBeingDragged) {
            if (dyConsumed != 0) {
                // The descendant was actually scrolled, so we won't bother it any longer.
                // It will receive all future events until it finished scrolling.
                nestedScrollTargetIsBeingDragged = true
                nestedScrollTargetWasUnableToScroll = false
            }
            else if (dyConsumed == 0 && dyUnconsumed != 0) {
                // The descendant tried scrolling in response to touch movements but was not able to do so.
                // We remember that in order to allow RecyclerView to take over scrolling.
                nestedScrollTargetWasUnableToScroll = true
                target.parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        if (axes and View.SCROLL_AXIS_VERTICAL != 0) {
            // A descendant started scrolling, so we'll observe it.
            nestedScrollTarget = target
            nestedScrollTargetIsBeingDragged = false
            nestedScrollTargetWasUnableToScroll = false
        }

        super.onNestedScrollAccepted(child, target, axes)
    }


    // We only support vertical scrolling.
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int) =
        (nestedScrollAxes and View.SCROLL_AXIS_VERTICAL != 0)


    override fun onStopNestedScroll(child: View) {
        // The descendant finished scrolling. Clean up!
        nestedScrollTarget = null
        nestedScrollTargetIsBeingDragged = false
        nestedScrollTargetWasUnableToScroll = false
    }
}