package tk.zwander.widgetdrawer.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_widget_select.*
import kotlinx.coroutines.*
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.adapters.AppListAdapter
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.ShortcutData
import tk.zwander.widgetdrawer.misc.WidgetInfo
import tk.zwander.widgetdrawer.views.Drawer


class WidgetSelectActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        const val PICK_CODE = 104
    }

    private val appWidgetManager by lazy { AppWidgetManager.getInstance(this) }
    private val adapter by lazy {
        AppListAdapter(this) {
            Drawer.onResult(this, Activity.RESULT_OK, PICK_CODE, Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, it)
            })

            finish()
        }
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

    private fun populateAsync() = launch {
        val apps = withContext(Dispatchers.Main) {
            val apps = HashMap<String, AppInfo>()

            appWidgetManager.installedProviders.forEach {
                val appInfo = packageManager.getApplicationInfo(it.provider.packageName, 0)

                val appName = packageManager.getApplicationLabel(appInfo)
                val widgetName = it.loadLabel(packageManager)

                var app = apps[appInfo.packageName]
                if (app == null) {
                    apps[appInfo.packageName] = AppInfo(appName.toString(), appInfo)
                    app = apps[appInfo.packageName]!!
                }

                app.widgets.add(WidgetInfo(widgetName,
                    it.previewImage.run { if (this != 0) this else appInfo.icon },
                    it, appInfo))
            }

            val shortcuts = packageManager.queryIntentActivities(
                Intent(Intent.ACTION_CREATE_SHORTCUT),
                PackageManager.GET_RESOLVED_FILTER
            )

            shortcuts.forEach {
                val appInfo = it.activityInfo.applicationInfo

                val appName = appInfo.loadLabel(packageManager)
                val shortcutName = it.loadLabel(packageManager)

                var app = apps[appInfo.packageName]
                if (app == null) {
                    val new = AppInfo(appName.toString(), appInfo)
                    apps[appInfo.packageName] = new
                    app = new
                }

                app!!.widgets.add(
                    WidgetInfo(
                        shortcutName.toString(),
                        it.activityInfo.iconResource,
                        ShortcutData(
                            shortcutName.toString(),
                            Intent.ShortcutIconResource()
                                .apply {
                                    packageName = appInfo.packageName
                                    resourceName = packageManager.getResourcesForApplication(appInfo)
                                        .getResourceName(it.iconResource)
                                },
                            it.activityInfo
                        ),
                        appInfo
                    )
                )
            }

            apps
        }

        adapter.addItems(apps.values)
        progress.visibility = View.GONE
        selection_list.visibility = View.VISIBLE
    }
}
