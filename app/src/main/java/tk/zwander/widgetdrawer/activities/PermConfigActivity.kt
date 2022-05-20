package tk.zwander.widgetdrawer.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ServiceManager
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.internal.appwidget.IAppWidgetService
import tk.zwander.widgetdrawer.host.WidgetHostCompat
import tk.zwander.widgetdrawer.misc.ShortcutData
import tk.zwander.widgetdrawer.utils.Event
import tk.zwander.widgetdrawer.utils.eventManager
import tk.zwander.widgetdrawer.views.Drawer

class PermConfigActivity : AppCompatActivity() {
    companion object {
        private const val CONFIGURE_REQ = 1000
    }

    private val widgetBindLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val id = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        eventManager.sendEvent(Event.PermissionResult(
            success = result.resultCode == Activity.RESULT_OK && id != -1,
            widgetId = id
        ))
        finish()
    }

    @Suppress("DEPRECATION")
    private val shortcutConfigLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = result.data?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)

        eventManager.sendEvent(Event.ShortcutConfigResult(
            success = intent != null,
            data = result.data?.getParcelableExtra(Drawer.EXTRA_SHORTCUT_DATA),
            intent = intent,
            name = result.data?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
            iconRes = result.data?.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE),
            iconBmp = result.data?.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
        ))
        finish()
    }

    private val configLauncher = ConfigureLauncher()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Drawer.ACTION_PERM -> {
                val permIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                permIntent.putExtras(intent.extras!!)
                widgetBindLauncher.launch(permIntent)
            }
            Drawer.ACTION_CONFIG -> {
                configLauncher.launch(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
            }
            Intent.ACTION_CREATE_SHORTCUT -> {
                val info = intent.getParcelableExtra<ShortcutData>(Drawer.EXTRA_SHORTCUT_DATA)
                val outIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)

                outIntent.`package` = info.activityInfo!!.packageName
                outIntent.component = ComponentName(info.activityInfo!!.packageName, info.activityInfo!!.name)

                shortcutConfigLauncher.launch(outIntent)
            }
        }
    }

    private inner class ConfigureLauncher {
        private val configLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            onActivityResult(CONFIGURE_REQ, result.resultCode, result.data)
        }

        @SuppressLint("NewApi")
        fun launch(id: Int): Boolean {
            //Use the system API instead of ACTION_APPWIDGET_CONFIGURE to try to avoid some permissions issues
            try {
                val intentSender = IAppWidgetService.Stub.asInterface(ServiceManager.getService(Context.APPWIDGET_SERVICE))
                    .createAppWidgetConfigIntentSender(opPackageName, id, 0)

                if (intentSender != null) {
                    configLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                    return true
                }
            } catch (_: Exception) {}

            try {
                WidgetHostCompat.getInstance(this@PermConfigActivity, 1003).startAppWidgetConfigureActivityForResult(
                    this@PermConfigActivity,
                    id, 0, CONFIGURE_REQ, null
                )
                return true
            } catch (_: Exception) {}

            return false
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == CONFIGURE_REQ) {
                val id = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

                eventManager.sendEvent(Event.WidgetConfigResult(
                    success = resultCode == Activity.RESULT_OK && id != -1,
                    widgetId = id ?: -1
                ))
            }
        }
    }
}
