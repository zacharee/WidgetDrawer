package tk.zwander.widgetdrawer

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.github.javiersantos.piracychecker.allow
import com.github.javiersantos.piracychecker.callback
import com.github.javiersantos.piracychecker.doNotAllow
import com.github.javiersantos.piracychecker.piracyChecker
import com.google.firebase.analytics.FirebaseAnalytics
import tk.zwander.widgetdrawer.activities.LicenseFailedDialog
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.PrefsManager

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    val prefs by lazy { PrefsManager.getInstance(this) }
    private val pChecker by lazy {
        piracyChecker {
            enableGooglePlayLicensing(resources.getString(R.string.lvl_key))

            callback {
                allow {
                    if (prefs.enabled)
                        DrawerService.start(this@App)

                    prefs.addPrefListener(this@App)
                }

                doNotAllow { piracyCheckerError, app ->
                    FirebaseAnalytics.getInstance(this@App).logEvent(
                        "failed_license",
                        Bundle().apply {
                            putString("msg", piracyCheckerError.toString())
                            putString("app", app?.packageName)
                        }
                    )

                    startActivity(Intent(this@App, LicenseFailedDialog::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        pChecker.start()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PrefsManager.ENABLED -> {
                if (prefs.enabled) DrawerService.start(this)
                else DrawerService.stop(this)
            }
        }
    }
}