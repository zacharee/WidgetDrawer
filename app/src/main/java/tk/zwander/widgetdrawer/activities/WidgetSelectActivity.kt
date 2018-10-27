package tk.zwander.widgetdrawer.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_widget_select.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.adapters.AppListAdapter
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.WidgetInfo
import tk.zwander.widgetdrawer.views.Drawer


class WidgetSelectActivity : AppCompatActivity() {
    companion object {
        const val PICK_CODE = 104
    }

    private val appWidgetManager by lazy { AppWidgetManager.getInstance(this) }
    private val adapter = AppListAdapter {
        Drawer.onResult(this, Activity.RESULT_OK, PICK_CODE, Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, it)
        })

        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_select)

        selection_list.adapter = adapter
        selection_list.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))

        Thread { populateAsync() }.start()
    }

    private fun populateAsync() {
        val apps = HashMap<String, AppInfo>()

        appWidgetManager.installedProviders.forEach {
            val appInfo = packageManager.getApplicationInfo(it.provider.packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo)
            val widgetName = it.loadLabel(packageManager)
            val appIcon = packageManager.getApplicationIcon(appInfo)
            val previewImg = it.loadPreviewImage(this, 0)

            var app = apps[appInfo.packageName]
            if (app == null) {
                apps[appInfo.packageName] = AppInfo(appName.toString(), appIcon)
                app = apps[appInfo.packageName]!!
            }
            app.widgets.add(WidgetInfo(widgetName, previewImg, it))
        }

        runOnUiThread {
            adapter.addItems(apps.values)
            progress.visibility = View.GONE
            selection_list.visibility = View.VISIBLE
        }
    }
}
