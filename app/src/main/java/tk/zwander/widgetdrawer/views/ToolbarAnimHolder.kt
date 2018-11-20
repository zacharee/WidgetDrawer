package tk.zwander.widgetdrawer.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.drawer_layout.view.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.utils.dpAsPx
import kotlin.math.absoluteValue

class ToolbarAnimHolder : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super (context, attributeSet)

    private val closedTranslation: Int
        get() = action_bar_wrapper.height
    private val openedTranslation = -context.dpAsPx(16)
    private val overdragLimit  = openedTranslation * 4
    private val touchListener = TouchListener()

    private var wasDragging = false
    private var isOpen = false
    private var currentlyTransitioning = false

    init {
        orientation = LinearLayout.VERTICAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        open_close_toolbar.setOnTouchListener(touchListener)
        action_bar_wrapper.setOnTouchListener(touchListener)
    }

    private fun transition(isOpen: Boolean = this.isOpen, duration: Long = Drawer.ANIM_DURATION) {
        if (!currentlyTransitioning) {
            currentlyTransitioning = true

            val newTranslation = if (isOpen) closedTranslation else openedTranslation

            animate()
                .translationY(newTranslation.toFloat())
                .setDuration(duration)
                .setInterpolator(if (isOpen) AnticipateInterpolator() as TimeInterpolator else OvershootInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        currentlyTransitioning = false

                        open_close_toolbar.animate()
                            .scaleY(if (isOpen) 1f else -1f)
                            .setDuration(Drawer.ANIM_DURATION)
                            .setInterpolator(if (isOpen) AnticipateInterpolator() as TimeInterpolator else OvershootInterpolator())
                            .start()
                    }
                })
                .start()

            this.isOpen = !isOpen
        }
    }

    private inner class TouchListener : OnTouchListener {
        private var prevY = -1f
        private var downY = -1f

        override fun onTouch(v: View, event: MotionEvent?): Boolean {
            v.onTouchEvent(event)

            return when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    prevY = event.rawY
                    downY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dist = prevY - event.rawY
                    prevY = event.rawY

                    if ((event.rawY - downY).absoluteValue > ViewConfiguration.get(context).scaledTouchSlop) {
                        wasDragging = true
                        val newTranslation = translationY - dist

                        if (newTranslation <= closedTranslation && newTranslation >= openedTranslation) {
                            translationY = newTranslation
                        } else if (newTranslation > closedTranslation) {
                            translationY = closedTranslation.toFloat()
                        } else if (newTranslation < openedTranslation) {
                            translationY -= dist / 2f //TODO make this an actual deceleration
                        }

                        true
                    } else false
                }

                MotionEvent.ACTION_UP -> {
                    if (wasDragging && translationY >= openedTranslation) {
                        transition(translationY > (closedTranslation / 3f), 100)
                    } else if (translationY < openedTranslation) {
                        transition(isOpen)
                    } else if (v.id != R.id.action_bar_wrapper) {
                        transition()
                        v.performClick()
                    }

                    wasDragging = false
                    true
                }
                else -> false
            }
        }
    }
}