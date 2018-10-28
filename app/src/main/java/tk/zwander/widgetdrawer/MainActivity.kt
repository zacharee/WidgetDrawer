package tk.zwander.widgetdrawer

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import tk.zwander.widgetdrawer.utils.PrefsManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.root, Prefs())
            ?.commit()
    }

    class Prefs : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                PrefsManager.ENABLED ->
                    (findPreference(PrefsManager.ENABLED) as SwitchPreferenceCompat).isChecked =
                            preferenceManager.sharedPreferences.getBoolean(PrefsManager.ENABLED, false)
            }
        }

        override fun onCreateAdapter(preferenceScreen: PreferenceScreen?): RecyclerView.Adapter<*> {
            return object : PreferenceGroupAdapter(preferenceScreen) {
                @SuppressLint("RestrictedApi")
                override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
                    super.onBindViewHolder(holder, position)
                    val preference = getItem(position)
                    if (preference is PreferenceCategory)
                        setZeroPaddingToLayoutChildren(holder.itemView)
                    else
                        holder.itemView.findViewById<View?>(R.id.icon_frame)?.visibility = if (preference.icon == null) View.GONE else View.VISIBLE
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

            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}
