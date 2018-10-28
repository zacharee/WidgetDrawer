package tk.zwander.widgetdrawer.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
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
import tk.zwander.widgetdrawer.utils.screenSize
import kotlin.math.absoluteValue

class Handle : LinearLayout, SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val MSG_LONG_PRESS = 0

        private const val LONG_PRESS_DELAY = 300
        private const val SWIPE_THRESHOLD = 50
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var onOpenListener: (() -> Unit)? = null

    private var inMoveMode = false
    private var screenWidth = -1

    private val gestureManager = GestureManager()
    private val wm by lazy { context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val prefs by lazy { PrefsManager(context) }

    private val handleLeft = resources.getDrawable(R.drawable.handle_left)
    private val handleRight = resources.getDrawable(R.drawable.handle_right)

    private val longClickHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                MSG_LONG_PRESS -> gestureManager.onLongPress()
            }
        }
    }

    val params = WindowManager.LayoutParams().apply {
        type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
                else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        width = context.dpAsPx(6)
        height = prefs.handleHeightPx.toInt()
        gravity = Gravity.TOP or prefs.handleSide
        y = prefs.handleYPx.toInt()
        format = PixelFormat.RGBA_8888
    }

    init {
        setSide()
        handleLeft.setTint(prefs.handleColor)
        handleRight.setTint(prefs.handleColor)
        isClickable = true
        isFocusable = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                screenWidth = context.screenSize().x
                longClickHandler.sendEmptyMessageAtTime(MSG_LONG_PRESS,
                    event.downTime + LONG_PRESS_DELAY)
            }
            MotionEvent.ACTION_UP -> {
                longClickHandler.removeMessages(MSG_LONG_PRESS)
                setMoveMove(false)
                prefs.handleYPx = params.y.toFloat()
            }
            MotionEvent.ACTION_MOVE -> {
                if (inMoveMode) {
                    val gravity = when {
                        event.rawX <= 1/3f * screenWidth -> {
                            PrefsManager.HANDLE_LEFT
                        }
                        event.rawX >= 2/3f * screenWidth -> {
                            PrefsManager.HANDLE_RIGHT
                        }
                        else -> -1
                    }
                    params.y = (event.rawY - params.height / 2f).toInt()
                    if (gravity != PrefsManager.HANDLE_UNCHANGED) {
                        params.gravity = Gravity.TOP or gravity
                        prefs.handleSide = gravity
                        setSide(gravity)
                    }
                    updateLayout()
                }
            }
        }
        return gestureManager.onTouchEvent(event)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefsManager.HANDLE_HEIGHT -> {

            }

            PrefsManager.HANDLE_COLOR -> {

            }
        }
    }

    private fun updateLayout(params: WindowManager.LayoutParams = this.params) {
        wm.updateViewLayout(this, params)
    }

    private fun setSide(gravity: Int = prefs.handleSide) {
        background = if (gravity == PrefsManager.HANDLE_RIGHT) handleRight
                        else handleLeft
    }

    private fun setMoveMove(inMoveMode: Boolean) {
        this.inMoveMode = inMoveMode
        val tint = if (inMoveMode)
            Color.argb(255, 120, 200, 255)
        else
            prefs.handleColor

        handleLeft.setTint(tint)
        handleRight.setTint(tint)
    }

    inner class GestureManager : GestureDetector.SimpleOnGestureListener() {
        val gestureDetector = GestureDetector(context, this, Handler(Looper.getMainLooper()))

        fun onTouchEvent(event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return if (distanceX.absoluteValue > distanceY.absoluteValue && !inMoveMode) {
                if ((distanceX > SWIPE_THRESHOLD && prefs.handleSide == PrefsManager.HANDLE_RIGHT)
                    || distanceX < -SWIPE_THRESHOLD) {
                    onOpenListener?.invoke()
                    true
                } else false
            } else false
        }

        fun onLongPress() {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(50)
            setMoveMove(true)
        }
    }
}