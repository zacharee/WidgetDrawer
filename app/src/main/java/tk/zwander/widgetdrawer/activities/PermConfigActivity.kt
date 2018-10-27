package tk.zwander.widgetdrawer.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import tk.zwander.widgetdrawer.views.Drawer

class PermConfigActivity : AppCompatActivity() {
    companion object {
        const val PERM_CODE = 102
        const val CONFIG_CODE = 103
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Drawer.onResult(this, resultCode, requestCode, data)
        finish()
    }

    private val buttons = ArrayList<Button>()

    private fun loopThrough(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            if (child is Button) buttons.add(child)
            else if (child is ViewGroup) loopThrough(child)
        }
    }
}
