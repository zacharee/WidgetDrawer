package tk.zwander.widgetdrawer.activities

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import tk.zwander.widgetdrawer.adapters.AppListAdapter
import tk.zwander.widgetdrawer.databinding.ActivityWidgetSelectBinding
import tk.zwander.widgetdrawer.misc.AppInfo
import tk.zwander.widgetdrawer.misc.ShortcutData
import tk.zwander.widgetdrawer.misc.WidgetInfo
import tk.zwander.widgetdrawer.utils.Event
import tk.zwander.widgetdrawer.utils.eventManager

class WidgetSelectActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(this) }
    private val adapter by lazy {
        AppListAdapter(this) {
            val event = when (it) {
                is AppWidgetProviderInfo -> Event.PickWidgetResult(
                    success = true,
                    providerInfo = it
                )
                is ShortcutData -> Event.PickShortcutResult(
                    success = true,
                    shortcutData = it
                )
                else -> null
            }

            event?.let {
                eventManager.sendEvent(event)
            }

            finish()
        }
    }
    private val binding by lazy { ActivityWidgetSelectBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.selectionList.adapter = adapter

        populateAsync()
    }

    override fun onBackPressed() {
        eventManager.sendEvent(Event.PickFailedResult)
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
        binding.progress.isVisible = false
        binding.selectionList.isVisible = true
    }
}
