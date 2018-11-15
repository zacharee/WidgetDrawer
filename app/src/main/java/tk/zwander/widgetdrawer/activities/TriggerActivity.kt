package tk.zwander.widgetdrawer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tk.zwander.widgetdrawer.services.DrawerService

class TriggerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DrawerService.openDrawer(this)

        finish()
    }
}
