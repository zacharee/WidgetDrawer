package tk.zwander.widgetdrawer.adapters

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import kotlinx.android.synthetic.main.widget_item.view.*
import tk.zwander.helperlib.toBitmap
import tk.zwander.widgetdrawer.R
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
            .setOnClickListener { selectionCallback.invoke(widgets.get(holder.adapterPosition).component) }
        holder.parseInfo(widgets.get(holder.adapterPosition), picasso)
    }

    override fun getItemCount() = widgets.size()

    fun addItem(item: WidgetInfo) {
        widgets.add(item)
        notifyDataSetChanged()
    }

    class WidgetVH(view: View) : RecyclerView.ViewHolder(view) {
        fun parseInfo(info: WidgetInfo, picasso: Picasso) {
            itemView.widget_name.text = info.widgetName

            val img = itemView.widget_image

            val remRes = itemView.context.packageManager.getResourcesForApplication(info.appInfo)

            val entryName = try {
                remRes.getResourceEntryName(info.previewImg)
            } catch (e: Exception) {
                null
            }

            val typeName = try {
                remRes.getResourceTypeName(info.previewImg)
            } catch (e: Exception) {
                null
            }

            picasso
                .load("android.resource://${info.appInfo.packageName}/$typeName/$entryName")
                .resize(img.maxWidth, img.maxHeight)
                .onlyScaleDown()
                .centerInside()
                .into(img, object : Callback {
                    override fun onError(e: Exception?) {
                        picasso
                            .load(Uri.parse("${AppIconRequestHandler.SCHEME}:${info.appInfo.packageName}"))
                            .resize(img.maxWidth, img.maxHeight)
                            .onlyScaleDown()
                            .centerInside()
                            .into(img)
                    }

                    override fun onSuccess() {}
                })
        }
    }


    class AppIconRequestHandler(private val context: Context) : RequestHandler() {
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
}