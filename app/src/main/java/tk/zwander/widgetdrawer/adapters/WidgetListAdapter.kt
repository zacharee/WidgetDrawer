package tk.zwander.widgetdrawer.adapters

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import tk.zwander.helperlib.toBitmap
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.databinding.WidgetItemBinding
import tk.zwander.widgetdrawer.misc.WidgetInfo


class WidgetListAdapter(private val picasso: Picasso, private val selectionCallback: (provider: Parcelable) -> Unit) :
    RecyclerView.Adapter<WidgetListAdapter.WidgetVH>() {
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
        holder.itemView
            .setOnClickListener { selectionCallback.invoke(widgets.get(holder.bindingAdapterPosition).component) }
        holder.parseInfo(widgets.get(holder.bindingAdapterPosition), picasso)
    }

    override fun getItemCount() = widgets.size()

    fun addItem(item: WidgetInfo) {
        widgets.add(item)
        notifyDataSetChanged()
    }

    class WidgetVH(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = WidgetItemBinding.bind(itemView)

        fun parseInfo(info: WidgetInfo, picasso: Picasso) {
            binding.widgetName.text = info.widgetName

            val img = binding.widgetImage
            binding.shortcutIndicator.isVisible = info.isShortcut

            picasso
                .load("${RemoteResourcesIconHandler.SCHEME}://${info.appInfo.packageName}/${info.previewImg}")
                .resize(img.maxWidth, img.maxHeight)
                .onlyScaleDown()
                .centerInside()
                .into(img, object : Callback {
                    override fun onError(e: Exception?) {
                        picasso
                            .load(Uri.parse("${AppIconRequestHandler.SCHEME}:${info.appInfo.packageName}"))
                            .resize(img.maxWidth, img.maxHeight)
                            .onlyScaleDown()
                            .into(img)
                    }

                    override fun onSuccess() {}
                })
        }
    }

    class AppIconRequestHandler(context: Context) : RequestHandler() {
        companion object {
            const val SCHEME = "package"
        }

        private val pm = context.packageManager

        override fun canHandleRequest(data: Request): Boolean {
            return (data.uri != null && data.uri.scheme == SCHEME)
        }

        override fun load(request: Request, networkPolicy: Int): Result? {
            val pName = request.uri.schemeSpecificPart

            val img = pm.getApplicationIcon(pName).toBitmap() ?: return null

            return Result(img, Picasso.LoadedFrom.DISK)
        }
    }

    class RemoteResourcesIconHandler(context: Context) : RequestHandler() {
        companion object {
            const val SCHEME = "remote_res_widget"
        }

        private val pm = context.packageManager

        override fun canHandleRequest(data: Request): Boolean {
            return (data.uri != null && data.uri.scheme == SCHEME)
        }

        override fun load(request: Request, networkPolicy: Int): Result? {
            val pathSegments = request.uri.pathSegments

            val pName = request.uri.host
            val id = pathSegments[0].toInt()
            val remRes = pm.getResourcesForApplication(pName)

            val img = ResourcesCompat.getDrawable(remRes, id, remRes.newTheme())?.toBitmap() ?: return null

            return Result(img, Picasso.LoadedFrom.DISK)
        }
    }
}