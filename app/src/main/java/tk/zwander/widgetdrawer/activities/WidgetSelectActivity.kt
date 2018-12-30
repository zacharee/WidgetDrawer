package tk.zwander.widgetdrawer.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_widget_select.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.adapters.AppListAdapter
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.ShortcutData
import tk.zwander.widgetdrawer.misc.WidgetInfo
import tk.zwander.widgetdrawer.utils.toBitmap
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
            val previewImg: Bitmap? =
                BitmapFactory.Options().run {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeResource(
                        packageManager.getResourcesForApplication(appInfo),
                        it.previewImage,
                        this
                    )

                    inSampleSize = getProperSampleSize(this, 512, 512)

                    inJustDecodeBounds = false
                    BitmapFactory.decodeResource(
                        packageManager.getResourcesForApplication(appInfo),
                        it.previewImage,
                        this
                    )
                } ?: appIcon.toBitmap()

            var app = apps[appInfo.packageName]
            if (app == null) {
                apps[appInfo.packageName] = AppInfo(appName.toString(), appIcon)
                app = apps[appInfo.packageName]!!
            }
            app.widgets.add(WidgetInfo(widgetName, previewImg, it))
        }

        val others = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_CREATE_SHORTCUT),
            PackageManager.GET_RESOLVED_FILTER
        )

        others.forEach {
            val appInfo = it.activityInfo.applicationInfo

            val appName = packageManager.getApplicationLabel(appInfo)
            val appIcon = packageManager.getApplicationIcon(appInfo)

            val shortcutName = it.loadLabel(packageManager)
            val shortcutIcon = BitmapFactory.decodeResource(
                packageManager.getResourcesForApplication(appInfo),
                it.activityInfo.iconResource
            ) ?: appIcon.toBitmap()

            var app = apps[appInfo.packageName]
            if (app == null) {
                val new = AppInfo(appName.toString(), appIcon)
                apps[appInfo.packageName] = new
                app = new
            }
            app!!.widgets.add(
                WidgetInfo(
                    shortcutName.toString(),
                    shortcutIcon,
                    ShortcutData(
                        shortcutName.toString(),
                        it.loadIcon(packageManager).toBitmap(),
                        it.activityInfo
                    )
                )
            )
        }

        runOnUiThread {
            adapter.addItems(apps.values)
            progress.visibility = View.GONE
            selection_list.visibility = View.VISIBLE
        }
    }

    private fun getProperSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSample = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSample >= reqHeight && halfWidth / inSample >= reqWidth) {
                inSample *= 2
            }
        }

        return inSample
    }
}
