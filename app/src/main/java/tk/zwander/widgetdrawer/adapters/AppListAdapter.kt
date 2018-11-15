package tk.zwander.widgetdrawer.adapters

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import kotlinx.android.synthetic.main.app_item.view.*
import kotlinx.android.synthetic.main.widget_item.view.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.WidgetInfo

class AppListAdapter(private val selectionCallback: (provider: Parcelable) -> Unit) : RecyclerView.Adapter<AppListAdapter.AppVH>() {
    private val items = SortedList(AppInfo::class.java, object : SortedList.Callback<AppInfo>() {
        override fun areItemsTheSame(item1: AppInfo?, item2: AppInfo?): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: AppInfo?, newItem: AppInfo?): Boolean {
            return false
        }

        override fun compare(o1: AppInfo, o2: AppInfo): Int {
            return o1.compareTo(o2)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AppVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.app_item,
                parent,
                false
            ), selectionCallback
        )

    override fun onBindViewHolder(holder: AppVH, position: Int) {
        holder.parseInfo(items.get(holder.adapterPosition))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount() = items.size()

    fun addItem(item: AppInfo) {
        items.add(item)
    }

    fun addItems(items: MutableCollection<AppInfo>) {
        items.forEach { addItem(it) }
    }

    class AppVH(view: View, selectionCallback: (provider: Parcelable) -> Unit) : RecyclerView.ViewHolder(view) {
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

    class WidgetListAdapter(private val selectionCallback: (provider: Parcelable) -> Unit) : RecyclerView.Adapter<WidgetListAdapter.WidgetVH>() {
        private val widgets = SortedList(WidgetInfo::class.java, object : SortedList.Callback<WidgetInfo>() {
            override fun areItemsTheSame(item1: WidgetInfo?, item2: WidgetInfo?): Boolean {
                return false
            }

            override fun areContentsTheSame(oldItem: WidgetInfo?, newItem: WidgetInfo?): Boolean {
                return false
            }

            override fun compare(o1: WidgetInfo, o2: WidgetInfo): Int {
                return o1.compareTo(o2)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemRangeChanged(position, count)
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }
        })

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            WidgetVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.widget_item,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: WidgetVH, position: Int) {
            holder.itemView.setOnClickListener { selectionCallback.invoke(widgets.get(holder.adapterPosition).component) }
            holder.parseInfo(widgets.get(holder.adapterPosition))
        }

        override fun getItemCount() = widgets.size()

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