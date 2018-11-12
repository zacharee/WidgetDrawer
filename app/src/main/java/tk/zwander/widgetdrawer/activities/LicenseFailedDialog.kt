package tk.zwander.widgetdrawer.activities

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import tk.zwander.widgetdrawer.R

class LicenseFailedDialog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.unlicensed)
            .setMessage(R.string.unlicensed_desc)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                System.exit(1)
            }
        dialog.show()
    }
}
