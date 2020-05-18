package tk.zwander.widgetdrawer.activities

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.ShortcutData
import tk.zwander.widgetdrawer.views.Drawer

class PermConfigActivity : AppCompatActivity() {
    companion object {
        const val PERM_CODE = 102
        const val CONFIG_CODE = 103
        const val SHORTCUT_CODE = 110
    }

    private val dummyHost by lazy { AppWidgetHost(this, 1003) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Drawer.ACTION_PERM -> {
                val permIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                permIntent.putExtras(intent.extras!!)
                startActivityForResult(permIntent, PERM_CODE)
            }
            Drawer.ACTION_CONFIG -> {
                dummyHost.startAppWidgetConfigureActivityForResult(this, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1), 0, CONFIG_CODE, null)
            }
            Intent.ACTION_CREATE_SHORTCUT -> {
                val info = intent.getParcelableExtra<ShortcutData>(Drawer.EXTRA_SHORTCUT_DATA)
                val outIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)

                outIntent.`package` = info.activityInfo!!.packageName
                outIntent.component = ComponentName(info.activityInfo!!.packageName, info.activityInfo!!.name)

                startActivityForResult(outIntent, SHORTCUT_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Drawer.onResult(this, resultCode, requestCode, data?.putExtras(intent))
        finish()
    }
}
