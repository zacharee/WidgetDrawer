package tk.zwander.widgetdrawer.views

import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlinx.android.synthetic.main.drawer_layout.view.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.utils.dpAsPx
import tk.zwander.widgetdrawer.utils.vibrate
import kotlin.math.absoluteValue

class ToolbarAnimHolder : LinearLayout {
    companion object {
        const val PREVIOUSLY_LT_THRESH = -1
        const val PREVIOUSLY_GT_THRESH = 1
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    private val closedTranslation: Int
        get() = action_bar_wrapper.height
    private val openedTranslation = -context.dpAsPx(16)
    private val threshold: Float
        get() = (openedTranslation + closedTranslation) / 2f
    private val touchListener = TouchListener()

    private val openAnim by lazy {
        SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, openedTranslation.toFloat()).apply {
            spring = SpringForce(openedTranslation.toFloat())
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
    }
    private val closeAnim by lazy {
        SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, closedTranslation.toFloat()).apply {
            spring = SpringForce(closedTranslation.toFloat())
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
    }

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

    private fun transition(isOpen: Boolean = this.isOpen) {
        if (!currentlyTransitioning) {
            var hasCalledAnim = false

            currentlyTransitioning = true

            (if (isOpen) closeAnim else openAnim).apply {
                addEndListener { _, _, _, _ ->
                    currentlyTransitioning = false
                    hasCalledAnim = false
                }
                addUpdateListener { _, _, _ ->
                    if (!hasCalledAnim && updateArrowForNewY()) hasCalledAnim = true
                }
            }.start()

            this.isOpen = !isOpen
        }
    }

    private fun updateArrowForNewY(): Boolean {
        val y = translationY

        return if (y < threshold) {
            animateArrowTo(true)
        } else {
            animateArrowTo(false)
        }
    }

    private fun animateArrowTo(isOpen: Boolean): Boolean {
        val dest = if (isOpen) -1f else 1f

        return if (open_close_toolbar.scaleY == dest) false
        else {
            open_close_toolbar.animate()
                .scaleY(dest)
                .setDuration(Drawer.ANIM_DURATION)
                .setInterpolator(if (isOpen) AnticipateInterpolator() as TimeInterpolator else OvershootInterpolator())
                .start()
            true
        }
    }

    private inner class TouchListener : OnTouchListener {
        private var prevY = -1f
        private var downY = -1f
        private var hasCalledAnim = false
        private var previously = 0

        override fun onTouch(v: View, event: MotionEvent?): Boolean {
            v.onTouchEvent(event)

            return when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    prevY = event.rawY
                    downY = event.rawY

                    if (!isOpen) context.vibrate(1)

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

                        if (translationY < threshold && previously != PREVIOUSLY_LT_THRESH) {
                            hasCalledAnim = false
                            previously = PREVIOUSLY_LT_THRESH
                        } else if (translationY >= threshold && previously != PREVIOUSLY_GT_THRESH) {
                            hasCalledAnim = false
                            previously = PREVIOUSLY_GT_THRESH
                        }

                        if (!hasCalledAnim && updateArrowForNewY()) {
                            hasCalledAnim = true
                        }

                        true
                    } else false
                }

                MotionEvent.ACTION_UP -> {
                    if (wasDragging && translationY >= openedTranslation) {
                        transition(translationY >= threshold)
                    } else if (translationY < openedTranslation) {
                        transition(isOpen)
                    } else if (v.id != R.id.action_bar_wrapper) {
                        transition()
                        v.performClick()
                    }

                    wasDragging = false
                    hasCalledAnim = false
                    true
                }
                else -> false
            }
        }
    }
}