package tk.zwander.widgetdrawer.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.utils.dpAsPx

class Handle : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var onOpenListener: (() -> Unit)? = null

    private val gestureManager = GestureManager()

    val params: WindowManager.LayoutParams
        get() = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
                    else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = context.dpAsPx(24)
            height = context.dpAsPx(64)
            gravity = Gravity.TOP or Gravity.RIGHT
            x = -(width / 2f).toInt()
            y = context.dpAsPx(64)
            format = PixelFormat.RGBA_8888
        }

    init {
        background = context.resources.getDrawable(R.drawable.handle_rect)
        background.setTint(Color.WHITE)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureManager.onTouchEvent(event)
    }

    inner class GestureManager : GestureDetector.SimpleOnGestureListener() {
        val gestureDetector = GestureDetector(context, this, null)

        fun onTouchEvent(event: MotionEvent?) = gestureDetector.onTouchEvent(event)

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) =
            if (distanceX > 50 && distanceX > distanceY) {
                onOpenListener?.invoke()
                true
            } else false
    }
}