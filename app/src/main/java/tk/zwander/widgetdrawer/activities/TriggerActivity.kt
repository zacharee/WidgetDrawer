package tk.zwander.widgetdrawer.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tk.zwander.widgetdrawer.R
import tk.zwander.widgetdrawer.services.DrawerService

class TriggerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
            val shortcutIntent = Intent(this, TriggerActivity::class.java)

            val resultIntent = Intent()
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, resources.getString(R.string.open_widget_drawer))

            val iconRes = Intent.ShortcutIconResource.fromContext(
                this, R.mipmap.ic_launcher
            )

            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            DrawerService.openDrawer(this)

            finish()
        }
    }
}
