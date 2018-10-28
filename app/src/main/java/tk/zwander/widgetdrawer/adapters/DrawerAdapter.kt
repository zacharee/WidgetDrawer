package tk.zwander.widgetdrawer.adapters

import android.appwidget.AppWidgetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.widget_holder.view.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.DrawerHost
import tk.zwander.widgetdrawer.misc.OverrideWidgetInfo
import tk.zwander.widgetdrawer.utils.PrefsManager
import tk.zwander.widgetdrawer.utils.dpAsPx

class DrawerAdapter(private val manager: AppWidgetManager,
                    private val appWidgetHost: DrawerHost,
                    private val prefs: PrefsManager) : RecyclerView.Adapter<DrawerAdapter.DrawerVH>() {
    companion object {
        const val SIZE_MIN = -5
        const val SIZE_DEF = -1
        const val SIZE_MAX = 4

        const val SIZE_STEP_PX = 100
    }

    private var isEditing = false
        set(value) {
            if (!value) deselectAll()
            field = value
        }

    val widgets = ArrayList<OverrideWidgetInfo>()

    override fun getItemCount() = widgets.size

    override fun getItemId(position: Int) = widgets[position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DrawerVH(LayoutInflater.from(parent.context).inflate(R.layout.widget_holder, parent, false))

    override fun onBindViewHolder(holder: DrawerVH, position: Int) {
        val widget = widgets[holder.adapterPosition]
        val info = manager.getAppWidgetInfo(widget.id)

        holder.itemView.selection.isChecked = isEditing && widget.isSelected
        holder.itemView.selection.visibility = if (isEditing) View.VISIBLE else View.GONE
        holder.itemView.selection.setOnClickListener { select(holder.adapterPosition) }
        (holder.itemView.widget_frame as CardView).apply {
//            if (childCount == 0) {
//                setBackgroundColor(resources.getColor(R.color.cardBackground))
//            }
            removeAllViews()
            addView(appWidgetHost.createView(
                holder.itemView.context,
                widget.id,
                info
            ))
        }
        holder.itemView.apply {
            layoutParams = (layoutParams as StaggeredGridLayoutManager.LayoutParams).apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = computeHeight(context.dpAsPx(info.minHeight), widget.forcedHeight)

                isFullSpan = widget.isFullWidth
            }
        }
    }

    fun addItem(widget: OverrideWidgetInfo) {
        widgets.add(widget)
        notifyItemInserted(widgets.lastIndex)
    }

    fun addAll(widgets: List<OverrideWidgetInfo>) {
        widgets.forEach {
            addItem(it)
        }
    }

    fun removeItem(widget: OverrideWidgetInfo) {
        val index = widgets.indexOf(widget)
        widgets.remove(widget)
        notifyItemRemoved(index)
    }

    fun removeAt(position: Int): OverrideWidgetInfo {
        val removed = widgets.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    fun showEdit() {
        isEditing = true
        notifyItemRangeChanged(0, widgets.lastIndex)
    }

    fun hideEdit() {
        isEditing = false
        notifyItemRangeChanged(0, widgets.lastIndex)
    }

    fun select(position: Int) {
        if (isEditing) {
            deselectAll(position)
            widgets[position].isSelected = true
            prefs.currentWidgets = widgets
        }
    }

    fun deselectAll(ignore: Int = -1) {
        widgets.forEach {
            val index = widgets.indexOf(it)

            if (it.isSelected && index != ignore) {
                it.isSelected = false
                notifyItemChanged(index)
            }
        }

        prefs.currentWidgets = widgets
    }

    private fun computeHeight(currentHeight: Int, expand: Int): Int {
        return when(expand) {
            SIZE_DEF -> currentHeight
            else -> currentHeight + ((expand + 1) * SIZE_STEP_PX)
        }
    }

    class DrawerVH(view: View) : RecyclerView.ViewHolder(view)
}