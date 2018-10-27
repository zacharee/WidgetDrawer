package tk.zwander.widgetdrawer.adapters

import android.appwidget.AppWidgetProviderInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.app_item.view.*
import kotlinx.android.synthetic.main.widget_item.view.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.WidgetInfo
import java.util.*

class AppListAdapter(private val selectionCallback: (provider: AppWidgetProviderInfo) -> Unit) : RecyclerView.Adapter<AppListAdapter.AppVH>() {
    private val items = TreeSet<AppInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AppVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.app_item,
                parent,
                false
            ), selectionCallback
        )

    override fun onBindViewHolder(holder: AppVH, position: Int) {
        holder.parseInfo(items.elementAt(holder.adapterPosition))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount() = items.size

    fun addItem(item: AppInfo) {
        items.add(item)
        notifyDataSetChanged()
    }

    fun addItems(items: MutableCollection<AppInfo>) {
        items.forEach { addItem(it) }
    }

    class AppVH(view: View, selectionCallback: (provider: AppWidgetProviderInfo) -> Unit) : RecyclerView.ViewHolder(view) {
        private val adapter = WidgetListAdapter(selectionCallback)

        fun parseInfo(info: AppInfo) {
            itemView.widget_holder.adapter = adapter
            itemView.widget_holder.addItemDecoration(DividerItemDecoration(itemView.context, RecyclerView.HORIZONTAL))

            itemView.app_name.text = info.appName
            itemView.app_icon.setImageDrawable(info.appIcon)
            info.widgets.forEach {
                adapter.addItem(it)
            }
        }
    }

    class WidgetListAdapter(private val selectionCallback: (provider: AppWidgetProviderInfo) -> Unit) : RecyclerView.Adapter<WidgetListAdapter.WidgetVH>() {
        private val widgets = TreeSet<WidgetInfo>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            WidgetVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.widget_item,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: WidgetVH, position: Int) {
            holder.itemView.setOnClickListener { selectionCallback.invoke(widgets.elementAt(holder.adapterPosition).component) }
            holder.parseInfo(widgets.elementAt(holder.adapterPosition))
        }

        override fun getItemCount() = widgets.size

        fun addItem(item: WidgetInfo) {
            widgets.add(item)
            notifyDataSetChanged()
        }

        class WidgetVH(view: View) : RecyclerView.ViewHolder(view) {
            fun parseInfo(info: WidgetInfo) {
                itemView.widget_name.text = info.widgetName
                itemView.widget_image.setImageDrawable(info.previewImg)
            }
        }
    }
}