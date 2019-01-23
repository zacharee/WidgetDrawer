package tk.zwander.widgetdrawer.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_widget_select.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tk.zwander.helperlib.toBitmap
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.adapters.AppListAdapter
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.ShortcutData
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

        populateAsync()
    }

    override fun onBackPressed() {
        Drawer.onResult(this, Activity.RESULT_CANCELED, PICK_CODE, null)

        finish()
    }

    private fun populateAsync() = GlobalScope.launch {
        val apps = HashMap<String, AppInfo>()

        appWidgetManager.installedProviders.forEach {
            val appInfo = packageManager.getApplicationInfo(it.provider.packageName, 0)

            val appName = packageManager.getApplicationLabel(appInfo)
            val widgetName = it.loadLabel(packageManager)
            val appIcon = packageManager.getApplicationIcon(appInfo)

            var app = apps[appInfo.packageName]
            if (app == null) {
                apps[appInfo.packageName] = AppInfo(appName.toString(), appIcon)
                app = apps[appInfo.packageName]!!
            }

            app.widgets.add(WidgetInfo(widgetName,
                it.previewImage.run { if (this != 0) this else appInfo.icon },
                it, appInfo))
        }

        val others = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_CREATE_SHORTCUT),
            PackageManager.GET_RESOLVED_FILTER
        )

        others.forEach {
            val appInfo = it.activityInfo.applicationInfo

            val appName = appInfo.loadLabel(packageManager)
            val appIcon = appInfo.loadIcon(packageManager)
            val shortcutName = it.loadLabel(packageManager)

            var app = apps[appInfo.packageName]
            if (app == null) {
                val new = AppInfo(appName.toString(), appIcon)
                apps[appInfo.packageName] = new
                app = new
            }

            app!!.widgets.add(
                WidgetInfo(
                    shortcutName.toString(),
                    it.activityInfo.iconResource,
                    ShortcutData(
                        shortcutName.toString(),
                        it.loadIcon(packageManager).toBitmap(),
                        it.activityInfo
                    ),
                    appInfo
                )
            )
        }

        runOnUiThread {
            adapter.addItems(apps.values)
            progress.visibility = View.GONE
            selection_list.visibility = View.VISIBLE
        }
    }
}
