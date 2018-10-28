package tk.zwander.widgetdrawer.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.utils.PrefsManager
import tk.zwander.widgetdrawer.utils.dpAsPx

class Handle : LinearLayout {
    companion object {
        private const val MSG_LONG_PRESS = 0

        private const val LONG_PRESS_DELAY = 300
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var onOpenListener: (() -> Unit)? = null

    private val gestureManager = GestureManager()
    private val wm by lazy { context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val prefs by lazy { PrefsManager(context) }

    val params = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
                else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        width = context.dpAsPx(6)
        height = context.dpAsPx(64)
        gravity = Gravity.TOP or Gravity.RIGHT
        y = prefs.handleYPx.toInt()
        format = PixelFormat.RGBA_8888
    }

    init {
        background = context.resources.getDrawable(R.drawable.handle_right)
        isClickable = true
        isFocusable = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureManager.onTouchEvent(event)
    }

    private fun updateLayout(params: WindowManager.LayoutParams = this.params) {
        wm.updateViewLayout(this, params)
    }

    inner class GestureManager : GestureDetector.SimpleOnGestureListener() {
        val gestureDetector = GestureDetector(context, this, Handler(Looper.getMainLooper()))

        private val longClickHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message?) {
                when (msg?.what) {
                    MSG_LONG_PRESS -> onLongPress()
                }
            }
        }

        private var inMoveMode = false

        fun onTouchEvent(event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    longClickHandler.sendEmptyMessageAtTime(MSG_LONG_PRESS,
                        event.downTime + LONG_PRESS_DELAY)
                }
                MotionEvent.ACTION_UP -> {
                    longClickHandler.removeMessages(MSG_LONG_PRESS)
                    inMoveMode = false
                    prefs.handleYPx = params.y.toFloat()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (inMoveMode) {
                        params.y = (event.rawY - params.height / 2f).toInt()
                        updateLayout()
                    }
                }
            }

            gestureDetector.onTouchEvent(event)

            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float) =
            if (distanceX > 50 && distanceX > distanceY && !inMoveMode) {
                onOpenListener?.invoke()
                true
            } else false

        private fun onLongPress() {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(50)
            inMoveMode = true
        }
    }
}