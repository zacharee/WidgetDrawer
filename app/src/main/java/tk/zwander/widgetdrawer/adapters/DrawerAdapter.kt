package tk.zwander.widgetdrawer.adapters

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.DrawerHost
import tk.zwander.widgetdrawer.misc.OverrideWidgetInfo
import tk.zwander.widgetdrawer.utils.dpAsPx
import tk.zwander.widgetdrawer.utils.screenSize

class DrawerAdapter(private val manager: AppWidgetManager,
                    private val appWidgetHost: DrawerHost,
                    private val removedCallback: (position: Int) -> Unit) : RecyclerView.Adapter<DrawerAdapter.DrawerVH>() {
    val widgets = ArrayList<OverrideWidgetInfo>()

    override fun getItemCount() = widgets.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            DrawerVH(LayoutInflater.from(parent.context).inflate(R.layout.widget_holder, parent, false))

    override fun onBindViewHolder(holder: DrawerVH, position: Int) {
        val widget = widgets[position]
        holder.setView(
            appWidgetHost.createView(
                holder.itemView.context,
                widget.id,
                manager.getAppWidgetInfo(widget.id)
            ),
            manager.getAppWidgetInfo(widget.id),
            widget
        )
        holder.setIsRecyclable(false)
    }

    fun addItem(widget: OverrideWidgetInfo) {
        widgets.add(widget)
        notifyItemInserted(widgets.lastIndex)
    }

    fun addAll(widgets: List<OverrideWidgetInfo>) {
        this.widgets.addAll(widgets)
        notifyDataSetChanged()
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

    class DrawerVH(view: View) : RecyclerView.ViewHolder(view) {
        fun setView(view: AppWidgetHostView,
                    info: AppWidgetProviderInfo,
                    widget: OverrideWidgetInfo) {
            (itemView as CardView).apply {
                addView(view)
                setBackgroundColor(resources.getColor(R.color.cardBackground))
                layoutParams = (layoutParams as StaggeredGridLayoutManager.LayoutParams).apply {
                    val proposedWidth = context.dpAsPx(info.minWidth)
                    val maxWidth = context.screenSize().x

                    width = if (widget.forcedWidth != -1) widget.forcedWidth else ViewGroup.LayoutParams.MATCH_PARENT
                    height = if (widget.forcedHeight != -1) widget.forcedHeight else context.dpAsPx(info.minHeight)

                    if (proposedWidth > maxWidth * 2f/3f) isFullSpan = true
                }
            }
        }
    }
}