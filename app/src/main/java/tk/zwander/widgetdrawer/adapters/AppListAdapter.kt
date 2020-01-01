package tk.zwander.widgetdrawer.adapters

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.app_item.view.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.DividerItemDecoration

class AppListAdapter(private val context: Context, private val selectionCallback: (provider: Parcelable) -> Unit) : RecyclerView.Adapter<AppListAdapter.AppVH>() {
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

    private val picasso = Picasso.Builder(context)
        .addRequestHandler(WidgetListAdapter.AppIconRequestHandler(context))
        .addRequestHandler(WidgetListAdapter.RemoteResourcesIconHandler(context))
        .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AppVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.app_item,
                parent,
                false
            ),
            picasso,
            selectionCallback
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

    class AppVH(view: View, private val picasso: Picasso, selectionCallback: (provider: Parcelable) -> Unit) : RecyclerView.ViewHolder(view) {
        private val adapter = WidgetListAdapter(picasso, selectionCallback)

        fun parseInfo(info: AppInfo) {
            itemView.widget_holder.adapter = adapter
            itemView.widget_holder.addItemDecoration(DividerItemDecoration(itemView.context, RecyclerView.HORIZONTAL))

            itemView.app_name.text = info.appName
            info.widgets.forEach {
                adapter.addItem(it)
            }

            picasso
                .load(Uri.parse("${WidgetListAdapter.AppIconRequestHandler.SCHEME}:${info.appInfo.packageName}"))
                .fit()
                .into(itemView.app_icon)
        }
    }
}