package tk.zwander.widgetdrawer.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.header_layout.view.*
import kotlinx.android.synthetic.main.shortcut_holder.view.*
import tk.zwander.helperlib.dpAsPx
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.BaseWidgetInfo
import tk.zwander.widgetdrawer.misc.DrawerHost
import tk.zwander.widgetdrawer.observables.EditingObservable
import tk.zwander.widgetdrawer.observables.SelectionObservable
import tk.zwander.widgetdrawer.observables.SizeObservable
import tk.zwander.widgetdrawer.observables.TransparentObservable
import tk.zwander.widgetdrawer.utils.*
import tk.zwander.widgetdrawer.views.CustomCard
import tk.zwander.widgetdrawer.views.Drawer
import java.util.*

class DrawerAdapter(
    private val manager: AppWidgetManager,
    private val appWidgetHost: DrawerHost
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val SIZE_DEF = -1

        const val SIZE_STEP_PX = 100

        const val TYPE_HEADER = 1
        const val TYPE_WIDGET = 0
        const val TYPE_SHORTCUT = 2
    }

    var isEditing = false
        set(value) {
            field = value
            if (!value) selectedId = -1
            editingObservable.setEditing(value)
        }
    var selectedId = -1
        set(value) {
            field = value
            selectedObservable.setSelection(value)
        }
    var transparentWidgets = false
        set(value) {
            field = value
            transparentObservable.setTransparent(value)
        }

    private val editingObservable = EditingObservable()
    private val selectedObservable = SelectionObservable()
    private val transparentObservable = TransparentObservable()
    val sizeObservable = SizeObservable()

    val widgets = ArrayList<BaseWidgetInfo>()
        .apply { add(0, BaseWidgetInfo.header()) }

    val selectedWidget: BaseWidgetInfo?
        get() = widgets.firstOrNull { it.id == selectedId }

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = widgets.size

    override fun getItemId(position: Int) = widgets[position].id.toLong()

    override fun getItemViewType(position: Int) =
        if (position == 0) TYPE_HEADER
        else {
            val widget = widgets[position]
            when (widget.type) {
                BaseWidgetInfo.TYPE_WIDGET -> TYPE_WIDGET
                BaseWidgetInfo.TYPE_SHORTCUT -> TYPE_SHORTCUT
                else -> throw IllegalArgumentException("Bad widget type")
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            TYPE_HEADER -> HeaderVH(LayoutInflater.from(parent.context).inflate(R.layout.header_layout, parent, false))
            TYPE_WIDGET -> {
                val vh = WidgetVH(LayoutInflater.from(parent.context).inflate(R.layout.widget_holder, parent, false))
                updateTransparency(vh, true)
                vh
            }
            TYPE_SHORTCUT -> {
                val vh = ShortcutVH(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.shortcut_holder,
                        parent,
                        false
                    )
                ) { motionEvent ->
                    parent.onTouchEvent(motionEvent)
                }
                updateTransparency(vh, true)
                vh
            }
            else -> throw IllegalArgumentException("Bad view type")
        }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BaseItemVH) {
            val widget = widgets[position]
            holder.onBind(widget)
        } else if (holder is HeaderVH) {
            holder.onBind()
        }
    }

    private fun updateTransparency(holder: BaseItemVH, forInit: Boolean) {
        val card = holder.widgetFrame

        val background = {
            val attr = intArrayOf(android.R.attr.colorBackground)
            val array = card.context.obtainStyledAttributes(attr)
            try {
                array.getColor(0, 0)
            } finally {
                array.recycle()
            }
        }.invoke()

        val elevation = card.context.resources
            .getDimensionPixelSize(R.dimen.elevation).toFloat()

        if (forInit) {
            card.setCardBackgroundColor(
                if (transparentWidgets) Color.TRANSPARENT
                else background
            )

            card.elevation =
                    if (transparentWidgets) 0f
                    else elevation

            return
        }

        val alphaAnim = ValueAnimator.ofArgb(
            if (transparentWidgets) background else Color.TRANSPARENT,
            if (transparentWidgets) Color.TRANSPARENT else background
        )
        alphaAnim.interpolator = if (transparentWidgets) AccelerateInterpolator() else DecelerateInterpolator()
        alphaAnim.duration = Drawer.ANIM_DURATION
        alphaAnim.addUpdateListener {
            card.setCardBackgroundColor(it.animatedValue.toString().toInt())
        }
        alphaAnim.start()

        val elevAnim = ValueAnimator.ofFloat(
            if (transparentWidgets) elevation else 0f,
            if (transparentWidgets) 0f else elevation
        )
        elevAnim.interpolator = if (transparentWidgets) AnticipateInterpolator() else OvershootInterpolator()
        elevAnim.duration = Drawer.ANIM_DURATION
        elevAnim.addUpdateListener {
            card.elevation = it.animatedValue.toString().toFloat()
        }
        elevAnim.start()
    }

    private fun updateSelectionCheck(holder: BaseItemVH, widget: BaseWidgetInfo) {
        holder.selection.isChecked = widget.id == selectedId
    }

    private fun updateSelectionVisibility(holder: BaseItemVH) {
        holder.selection.apply {
            if (isEditing) {
                visibility = View.VISIBLE
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(Drawer.ANIM_DURATION)
                    .setInterpolator(OvershootInterpolator())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            scaleX = 1f
                            scaleY = 1f
                        }
                    })
            } else {
                animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(Drawer.ANIM_DURATION)
                    .setInterpolator(AnticipateInterpolator())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            visibility = View.GONE
                            scaleX = 0f
                            scaleY = 0f
                        }
                    })
            }
        }
    }

    private fun updateDimens(holder: WidgetVH, info: AppWidgetProviderInfo?, widget: BaseWidgetInfo) {
        if (info == null) return
        holder.itemView.apply {
            layoutParams = (layoutParams as StaggeredGridLayoutManager.LayoutParams).apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                val h = computeHeight(context.dpAsPx(info.minHeight), widget.forcedHeight)
                if (validLowerHeight(h))
                    height = h
                else {
                    widget.forcedHeight += 1
                    height = computeHeight(context.dpAsPx(info.minHeight), widget.forcedHeight)
                    PrefsManager.getInstance(context).currentWidgets = widgets
                }

                isFullSpan = widget.isFullWidth
            }
        }
    }

    private fun updateDimens(holder: ShortcutVH, widget: BaseWidgetInfo) {
        holder.itemView.apply {
            layoutParams = (layoutParams as StaggeredGridLayoutManager.LayoutParams).apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                val h = computeHeight(context.dpAsPx(100), widget.forcedHeight)
                if (validLowerHeight(h))
                    height = h
                else {
                    widget.forcedHeight += 1
                    height = computeHeight(context.dpAsPx(100), widget.forcedHeight)
                    PrefsManager.getInstance(context).currentWidgets = widgets
                }

                isFullSpan = widget.isFullWidth
            }
        }
    }

    fun addItem(widget: BaseWidgetInfo) {
        widgets.add(widget)
        notifyItemInserted(widgets.lastIndex)
    }

    fun addAt(index: Int, widget: BaseWidgetInfo) {
        widgets.add(index, widget)
        notifyItemInserted(index)
    }

    fun addAll(widgets: List<BaseWidgetInfo>) {
        widgets.forEach {
            addItem(it)
        }
    }

    fun setAll(widgets: List<BaseWidgetInfo>) {
        this.widgets.removeAll { it.type != BaseWidgetInfo.TYPE_HEADER }
        this.widgets.addAll(widgets)
        notifyDataSetChanged()
    }

    fun removeItem(widget: BaseWidgetInfo) {
        val index = widgets.indexOf(widget)
        widgets.remove(widget)
        notifyItemRemoved(index)
    }

    fun removeAt(position: Int): BaseWidgetInfo {
        val removed = widgets.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    private fun validLowerHeight(currentHeight: Int) = currentHeight > 100

    private fun computeHeight(currentHeight: Int, expand: Int): Int {
        return when (expand) {
            SIZE_DEF -> currentHeight
            else -> currentHeight + ((expand + 1) * SIZE_STEP_PX)
        }
    }

    inner class WidgetVH(view: View) : BaseItemVH(view) {
        init {
            sizeObservable.addObserver { _, arg ->
                if (adapterPosition != -1) {
                    val currentWidgetInfo = widgets[adapterPosition]

                    if (arg == currentWidgetInfo.id) {
                        updateDimens(this, getWidgetInfo(currentWidgetInfo.id), currentWidgetInfo)
                    }
                }
            }
        }

        override fun onBind(widget: BaseWidgetInfo) {
            super.onBind(widget)

            val widgetInfo = getWidgetInfo(widget.id)

            val view = appWidgetHost.createView(
                itemView.context,
                widget.id,
                widgetInfo
            )

            view.setOnClickListener {
                val newInfo = widgets[adapterPosition]

                if (isEditing) {
                    selectedId = newInfo.id
                    this@WidgetVH.selection.isChecked = true
                }
            }

            widgetFrame.apply {
                removeAllViews()
                addView(view)
            }

            updateDimens(
                this@WidgetVH,
                widgetInfo,
                widget
            )

            itemView.post {
                val width = itemView.context.pxAsDp(itemView.width).toInt()
                val height = itemView.context.pxAsDp(itemView.height).toInt()

                view.updateAppWidgetSize(null, width, height, width, height)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class ShortcutVH(view: View, private val scrollCallback: (MotionEvent) -> Unit) : BaseItemVH(view) {
        var name: String?
            get() = itemView.shortcut_label.text.toString()
            set(value) {
                itemView.shortcut_label.text = value
            }
        var icon: Drawable?
            get() = itemView.shortcut_icon.drawable
            set(value) {
                itemView.shortcut_icon.setImageDrawable(value)
            }

        private var wasScroll = false
        private val gestureDetector =
            GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                    val newEvent = MotionEvent.obtain(e2)

                    scaleMotionEvent(newEvent)

                    scrollCallback.invoke(newEvent)

                    newEvent.recycle()
                    wasScroll = true
                    return true
                }
            })

        init {
            itemView.setOnTouchListener { _, event ->
                val scrollCheck = (event.action == MotionEvent.ACTION_UP && wasScroll)
                if (scrollCheck) wasScroll = false
                gestureDetector.onTouchEvent(event) || scrollCheck
            }

            sizeObservable.addObserver { _, arg ->
                if (adapterPosition != -1) {
                    val currentShortcut = widgets[adapterPosition]

                    updateDimens(this, currentShortcut)
                }
            }
        }

        override fun onBind(widget: BaseWidgetInfo) {
            super.onBind(widget)

            name = widget.label
            icon = widget.iconBmpEncoded.base64ToBitmap()?.toBitmapDrawable(itemView.resources) ?: widget.iconRes?.loadToDrawable(itemView.context)
            itemView.setOnClickListener {
                selection.performClick()

                if (!isEditing) {
                    itemView.context.startActivity(
                        widget.shortcutIntent
                            ?.also { intent -> intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            ?: return@setOnClickListener)
                }
            }

            updateDimens(this, widgets[adapterPosition])
        }

        private fun scaleMotionEvent(event: MotionEvent) {
            event.setLocation(
                event.x + itemView.left + (itemView.parent as ViewGroup).left,
                event.y + itemView.top + (itemView.parent as ViewGroup).top
            )
        }
    }

    open inner class BaseItemVH(view: View) : RecyclerView.ViewHolder(view) {
        val selection: RadioButton
            get() = itemView.findViewById(R.id.selection)
        val widgetFrame: CustomCard
            get() = itemView.findViewById(R.id.widget_frame)

        init {
            editingObservable.addObserver { _, _ ->
                updateSelectionVisibility(this)
            }

            selectedObservable.addObserver { _, _ ->
                if (adapterPosition != -1) updateSelectionCheck(
                    this,
                    widgets[adapterPosition]
                )
            }

            transparentObservable.addObserver { _, _ ->
                updateTransparency(this, false)
            }

            selection.setOnClickListener { if (isEditing) selectedId = widgets[adapterPosition].id }
        }

        fun getWidgetInfo(id: Int): AppWidgetProviderInfo? = manager.getAppWidgetInfo(id)
        
        open fun onBind(widget: BaseWidgetInfo) {
            updateSelectionCheck(this, widget)
        }
    }

    inner class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        init {
            (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        }

        fun onBind() {
            editingObservable.addObserver { _, _ ->
                val height = itemView.edit_instructions
                    .apply { measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) }
                    .measuredHeight

                ValueAnimator.ofInt(itemView.height, if (isEditing) height else 0)
                    .apply {
                        interpolator = if (isEditing) DecelerateInterpolator() as TimeInterpolator
                        else AccelerateInterpolator()

                        addUpdateListener {
                            itemView.layoutParams.apply {
                                this.height = it.animatedValue.toString().toInt()

                                itemView.layoutParams = this
                            }
                        }
                    }
                    .start()
            }
        }
    }
}