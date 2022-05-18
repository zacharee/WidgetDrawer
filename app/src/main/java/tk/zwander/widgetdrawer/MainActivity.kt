package tk.zwander.widgetdrawer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tk.zwander.widgetdrawer.services.DrawerService
import tk.zwander.widgetdrawer.utils.Event
import tk.zwander.widgetdrawer.utils.PrefsManager
import tk.zwander.widgetdrawer.utils.accessibilityEnabled
import tk.zwander.widgetdrawer.utils.eventManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root, Prefs())
            .commit()
    }

    class Prefs : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

            findPreference<Preference>("open_drawer")?.setOnPreferenceClickListener {
                requireContext().eventManager.sendEvent(Event.ShowDrawer())
                true
            }

            findPreference<SwitchPreference>("enhanced_view_mode")?.setOnPreferenceChangeListener { pref, newValue ->
                if (newValue.toString().toBoolean() && !requireContext().accessibilityEnabled) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.enhanced_view_mode)
                        .setMessage(R.string.enhanced_view_mode_grant_desc)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            Toast.makeText(requireContext(), R.string.enable_accessibility, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            (pref as SwitchPreference).isChecked = false
                        }
                        .setOnCancelListener {
                            (pref as SwitchPreference).isChecked = false
                        }
                        .show()
                }

                true
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                PrefsManager.ENABLED ->
                    findPreference<SwitchPreference>(PrefsManager.ENABLED)?.isChecked =
                            preferenceManager.sharedPreferences?.getBoolean(PrefsManager.ENABLED, false) ?: false
            }
        }

        @SuppressLint("RestrictedApi")
        override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
            return object : PreferenceGroupAdapter(preferenceScreen) {
                @SuppressLint("RestrictedApi")
                override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
                    super.onBindViewHolder(holder, position)
                    val preference = getItem(position)
                    if (preference is PreferenceCategory)
                        setZeroPaddingToLayoutChildren(holder.itemView)
                    else
                        holder.itemView.findViewById<View?>(R.id.icon_frame)?.visibility = if (preference?.icon == null) View.GONE else View.VISIBLE
                }
            }
        }

        private fun setZeroPaddingToLayoutChildren(view: View) {
            if (view !is ViewGroup)
                return
            val childCount = view.childCount
            for (i in 0 until childCount) {
                setZeroPaddingToLayoutChildren(view.getChildAt(i))
                view.setPaddingRelative(0, view.paddingTop, view.paddingEnd, view.paddingBottom)
            }
        }

        override fun onDestroy() {
            super.onDestroy()

            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}
