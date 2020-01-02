package tk.zwander.widgetdrawer.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.*
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View.OnClickListener
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager
import kotlinx.android.synthetic.main.drawer_layout.view.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.activities.PermConfigActivity
import tk.zwander.widgetdrawer.activities.PermConfigActivity.Companion.CONFIG_CODE
import tk.zwander.widgetdrawer.activities.PermConfigActivity.Companion.PERM_CODE
import tk.zwander.widgetdrawer.activities.PermConfigActivity.Companion.SHORTCUT_CODE
import tk.zwander.widgetdrawer.activities.WidgetSelectActivity
import tk.zwander.widgetdrawer.activities.WidgetSelectActivity.Companion.PICK_CODE
import tk.zwander.widgetdrawer.adapters.DrawerAdapter
import tk.zwander.widgetdrawer.misc.BaseWidgetInfo
import tk.zwander.widgetdrawer.misc.DrawerHost
import tk.zwander.widgetdrawer.misc.ShortcutData
import tk.zwander.widgetdrawer.misc.ShortcutIdManager
import tk.zwander.widgetdrawer.utils.PrefsManager
import tk.zwander.widgetdrawer.utils.screenSize
import tk.zwander.widgetdrawer.utils.toBase64
import java.util.*


class Drawer : FrameLayout, SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val ACTION_PERM = "PERMISSION"
        const val ACTION_CONFIG = "CONFIGURATION"

        const val ACTION_RESULT = "PICK_WIDGET"

        const val EXTRA_CODE = "code"
        const val EXTRA_DATA = "data"
        const val EXTRA_SHORTCUT_DATA = "shortcut_data"
        const val EXTRA_APPWIDGET_CONFIGURE = "configure"

        const val ANIM_DURATION = 200L
        const val UNDO_DURATION = 3000L

        fun onResult(context: Context, result: Int, code: Int, data: Intent?) {
            val intent = Intent(ACTION_RESULT)
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, result)
            intent.putExtra(EXTRA_CODE, code)
            intent.putExtra(EXTRA_DATA, data)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var hideListener: (() -> Unit)? = null

    val params: WindowManager.LayoutParams
        get() = WindowManager.LayoutParams().apply {
            val displaySize = context.screenSize()
            type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
            else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
//            dimAmount = 0.5f
            width = displaySize.x
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = PixelFormat.RGBA_8888
            gravity = Gravity.TOP
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

    val snackbarManager = SnackProgressBarManager(this).apply {

    }

    private val wm by lazy { context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val host by lazy { DrawerHost(context.applicationContext, 1003, this) }
    private val manager by lazy { AppWidgetManager.getInstance(context.applicationContext) }
    private val shortcutIdManager by lazy { ShortcutIdManager.getInstance(context) }
    private val prefs by lazy { PrefsManager.getInstance(context) }
    private val adapter by lazy { DrawerAdapter(manager, host) }

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                ACTION_RESULT -> onActivityResult(
                    intent.getIntExtra(EXTRA_CODE, -1000),
                    intent.getIntExtra(Intent.EXTRA_RETURN_RESULT, -1000),
                    intent.getParcelableExtra(EXTRA_DATA)
                )
            }
        }
    }

    private val globalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> {
                    hideDrawer()
                }
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            add_widget.setOnClickListener { pickWidget() }
            close_drawer.setOnClickListener { hideDrawer() }
            toggle_transparent.setOnClickListener {
                prefs.transparentWidgets = !prefs.transparentWidgets
                adapter.transparentWidgets = prefs.transparentWidgets
            }

            widget_grid.onMoveListener = { _, viewHolder, target ->
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                if (toPosition == 0 || fromPosition == 0) false
                else {
                    if (fromPosition < toPosition) {
                        for (i in fromPosition until toPosition) {
                            Collections.swap(adapter.widgets, i, i + 1)
                        }
                    } else {
                        for (i in fromPosition downTo toPosition + 1) {
                            Collections.swap(adapter.widgets, i, i - 1)
                        }
                    }

                    adapter.notifyItemMoved(fromPosition, toPosition)

                    prefs.currentWidgets = adapter.widgets

                    true
                }
            }

            widget_grid.onSwipeListener = { viewHolder, _ ->
                removeWidget(viewHolder.adapterPosition)
            }

            val inAnim = AlphaAnimation(0f, 1f).apply {
                duration = ANIM_DURATION
                interpolator = DecelerateInterpolator()
            }
            val outAnim = AlphaAnimation(1f, 0f).apply {
                duration = ANIM_DURATION
                interpolator = AccelerateInterpolator()
            }

            val animListener = object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    widget_grid.allowReorder = adapter.isEditing
                }
            }

            inAnim.setAnimationListener(animListener)
            outAnim.setAnimationListener(animListener)

            action_bar_wrapper.inAnimation = inAnim
            action_bar_wrapper.outAnimation = outAnim

            action_bar_wrapper.layoutAnimationListener

            edit.setOnClickListener {
                adapter.isEditing = true
                action_bar_wrapper.showNext()
            }

            go_back.setOnClickListener {
                adapter.isEditing = false
                action_bar_wrapper.showPrevious()
            }

            val listener = OnClickListener { view ->
                adapter.selectedWidget?.let { widget ->
                    var changed = false

                    when (view.id) {
                        R.id.expand_horiz -> {
                            changed = widget.isFullWidth != true
                            widget.isFullWidth = true
                        }
                        R.id.collapse_horiz -> {
                            changed = widget.isFullWidth != false
                            widget.isFullWidth = false
                        }
                        R.id.expand_vert -> {
                            changed = true
                            widget.forcedHeight++
                        }
                        R.id.collapse_vert -> {
                            changed = true
                            widget.forcedHeight--
                        }
                    }

                    if (changed) {
                        prefs.currentWidgets = adapter.widgets
                        adapter.sizeObservable.setSize(widget.id)
                    }
                }
            }

            expand_horiz.setOnClickListener(listener)
            expand_vert.setOnClickListener(listener)
            collapse_horiz.setOnClickListener(listener)
            collapse_vert.setOnClickListener(listener)

            adapter.transparentWidgets = prefs.transparentWidgets
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        handler?.postDelayed({
            val anim = ValueAnimator.ofFloat(0f, 1f)
            anim.interpolator = DecelerateInterpolator()
            anim.duration = ANIM_DURATION
            anim.addUpdateListener {
                alpha = it.animatedValue.toString().toFloat()
            }
            anim.start()
        }, 10)

        setBackgroundColor(prefs.drawerBg)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefsManager.TRANSPARENT_WIDGETS -> adapter.transparentWidgets = prefs.transparentWidgets
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK) {
            hideDrawer()
            return true
        }

        return super.dispatchKeyEvent(event)
    }

    fun onCreate() {
        host.startListening()
        LocalBroadcastManager.getInstance(context).registerReceiver(localReceiver, IntentFilter().apply {
            addAction(ACTION_RESULT)
        })
        context.registerReceiver(globalReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        })
        prefs.addPrefListener(this)

        widget_grid.adapter = adapter
        widget_grid.isNestedScrollingEnabled = true
        widget_grid.setHasFixedSize(true)
        (widget_grid.layoutManager as StaggeredGridLayoutManager).apply {
            spanCount = 2
        }
        adapter.addAll(prefs.currentWidgets)
    }

    fun onDestroy() {
        hideDrawer(false)
        host.stopListening()
        prefs.currentWidgets = adapter.widgets

        LocalBroadcastManager.getInstance(context).unregisterReceiver(localReceiver)
        context.unregisterReceiver(globalReceiver)
        prefs.removePrefListener(this)
    }

    fun showDrawer() {
        try {
            wm.addView(this, params)
        } catch (e: Exception) {
        }
    }

    fun hideDrawer(callListener: Boolean = true) {
        val anim = ValueAnimator.ofFloat(1f, 0f)
        anim.interpolator = AccelerateInterpolator()
        anim.duration = ANIM_DURATION
        anim.addUpdateListener {
            alpha = it.animatedValue.toString().toFloat()
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (callListener) hideListener?.invoke()
                handler?.postDelayed({
                    try {
                        wm.removeView(this@Drawer)
                    } catch (e: Exception) {
                    }
                }, 10)
            }
        })
        anim.start()
    }

    private fun getWidgetPermission(id: Int, componentName: ComponentName, options: Bundle? = null) {
        val intent = Intent(ACTION_PERM)
        intent.component = ComponentName(context, PermConfigActivity::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, componentName)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        context.startActivity(intent)
    }

    private fun configureWidget(
        id: Int,
        configure: ComponentName
    ) {
        if (context.packageManager.getActivityInfo(configure, 0).exported) {
            val intent = Intent(ACTION_CONFIG)
            intent.putExtra(EXTRA_APPWIDGET_CONFIGURE, configure)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            intent.component = ComponentName(context, PermConfigActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.unable_to_configure_widget, Toast.LENGTH_SHORT).show()
            addNewWidget(id)
        }
    }

    private fun pickWidget() {
        hideDrawer()
        val intent = Intent(context, WidgetSelectActivity::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, host.allocateAppWidgetId())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun tryBindWidget(info: AppWidgetProviderInfo, id: Int = host.allocateAppWidgetId()) {
        val canBind = manager.bindAppWidgetIdIfAllowed(id, info.provider)

        if (!canBind) getWidgetPermission(id, info.provider)
        else {
            if (info.configure != null && !adapter.widgets.map { it.id }.contains(id)) {
                configureWidget(id, info.configure)
            } else {
                addNewWidget(id)
            }
        }
    }

    private fun tryBindShortcut(info: ShortcutData) {
        val intent = Intent(context, PermConfigActivity::class.java)
        intent.action = Intent.ACTION_CREATE_SHORTCUT
        intent.putExtra(EXTRA_SHORTCUT_DATA, info)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(intent)
    }

    private fun addNewWidget(id: Int) {
        showDrawer()
        val info = createSavedWidget(id)
        adapter.addItem(info)
        prefs.currentWidgets = adapter.widgets
    }

    private fun addNewShortcut(info: BaseWidgetInfo) {
        showDrawer()
        adapter.addItem(info)
        prefs.currentWidgets = adapter.widgets
    }

    private fun createSavedWidget(id: Int): BaseWidgetInfo {
        return BaseWidgetInfo.widget(id)
    }

    private fun removeWidget(position: Int) {
        val info = adapter.removeAt(position)

        val timer = object : CountDownTimer(UNDO_DURATION, 1) {
            override fun onFinish() {
                snackbarManager.dismiss()

                if (info.type == BaseWidgetInfo.TYPE_WIDGET) host.deleteAppWidgetId(info.id)
                else if (info.type == BaseWidgetInfo.TYPE_SHORTCUT) shortcutIdManager.removeShortcutId(info.id)
                prefs.currentWidgets = adapter.widgets
            }

            override fun onTick(millisUntilFinished: Long) {
                snackbarManager.setProgress((UNDO_DURATION - millisUntilFinished).toInt())
            }
        }

        val snackbar = SnackProgressBar(
            SnackProgressBar.TYPE_HORIZONTAL,
            resources.getString(R.string.widget_removed)
        )

        snackbar.setAllowUserInput(true)
        snackbar.setSwipeToDismiss(true)
        snackbar.setIsIndeterminate(false)
        snackbar.setProgressMax(UNDO_DURATION.toInt())

        snackbar.setAction(resources.getString(R.string.undo), object : SnackProgressBar.OnActionClickListener {
            override fun onActionClick() {
                adapter.addAt(position, info)
                timer.cancel()
            }
        })

        snackbarManager.show(snackbar, SnackProgressBarManager.LENGTH_INDEFINITE, 100)
        timer.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PERM_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    tryBindWidget(
                        manager.getAppWidgetInfo(
                            data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: return
                        )
                    )
                }
            }
            CONFIG_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val id = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: return
                    if (id == -1) return
                    addNewWidget(id)
                } else
                    showDrawer()
            }
            PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val res = data?.getParcelableExtra<Parcelable>(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER)

                    if (res is AppWidgetProviderInfo) tryBindWidget(res)
                    else if (res is ShortcutData) tryBindShortcut(res)
                } else
                    showDrawer()
            }
            SHORTCUT_CODE -> {
                val intent = data?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)

                if (intent != null) {
                    val info = data.getParcelableExtra<ShortcutData>(EXTRA_SHORTCUT_DATA)
                    val name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
                    val iconRes =
                        data.getParcelableExtra<Intent.ShortcutIconResource?>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
                    val iconBmp = data.getParcelableExtra<Bitmap?>(Intent.EXTRA_SHORTCUT_ICON)

                    val shortcut = BaseWidgetInfo.shortcut(
                        name ?: info?.label,
                        iconBmp.toBase64(),
                        iconRes ?: info?.iconRes,
                        shortcutIdManager.allocateShortcutId(),
                        intent
                    )

                    addNewShortcut(shortcut)
                }
            }
        }
    }
}