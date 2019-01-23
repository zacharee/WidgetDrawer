package tk.zwander.widgetdrawer.activities

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.misc.ShortcutData
import tk.zwander.widgetdrawer.views.Drawer

class PermConfigActivity : AppCompatActivity() {
    companion object {
        const val PERM_CODE = 102
        const val CONFIG_CODE = 103
        const val SHORTCUT_CODE = 110
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Drawer.ACTION_PERM -> {
                val permIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                permIntent.putExtras(intent.extras!!)
                startActivityForResult(permIntent, PERM_CODE)
            }
            Drawer.ACTION_CONFIG -> {
                val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                configIntent.component = intent.getParcelableExtra(Drawer.EXTRA_APPWIDGET_CONFIGURE)
                configIntent.putExtras(intent.extras!!)
                startActivityForResult(configIntent, CONFIG_CODE)
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
