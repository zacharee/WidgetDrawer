package tk.zwander.widgetdrawer.misc;

import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.jaredrummler.android.colorpicker.ColorPanelView
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jaredrummler.android.colorpicker.ColorShape
import tk.zwander.widgetdrawer.R

/**
 * A Preference to select a color
 * https://github.com/jaredrummler/ColorPicker/blob/master/library_compat/src/main/java/com/jaredrummler/android/colorpicker/ColorPreferenceCompat.java
 */
class ColorPreferenceCompat : Preference, ColorPickerDialogListener {

    private var onShowDialogListener: OnShowDialogListener? = null
    private var color = Color.BLACK
    private var showDialog: Boolean = false
    @ColorPickerDialog.DialogType
    private var dialogType: Int = 0
    private var colorShape: Int = 0
    private var allowPresets: Boolean = false
    private var allowCustom: Boolean = false
    private var showAlphaSlider: Boolean = false
    private var showColorShades: Boolean = false
    private var previewSize: Int = 0
    /**
     * Get the colors that will be shown in the [ColorPickerDialog].
     *
     * @return An array of color ints
     */
    /**
     * Set the colors shown in the [ColorPickerDialog].
     *
     * @param presets An array of color ints
     */
    var presets: IntArray? = null
    private var dialogTitle: Int = 0

    val activity: FragmentActivity
        get() {
            val context = context
            if (context is FragmentActivity) {
                return context
            } else if (context is ContextWrapper) {
                val baseContext = context.baseContext
                if (baseContext is FragmentActivity) {
                    return baseContext
                }
            }
            throw IllegalStateException("Error getting activity from context")
        }

    /**
     * The tag used for the [ColorPickerDialog].
     *
     * @return The tag
     */
    val fragmentTag: String
        get() = "color_$key"

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        isPersistent = true
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorPreference)
        showDialog = a.getBoolean(R.styleable.ColorPreference_cpv_showDialog, true)

        dialogType = a.getInt(R.styleable.ColorPreference_cpv_dialogType, ColorPickerDialog.TYPE_PRESETS)
        colorShape = a.getInt(R.styleable.ColorPreference_cpv_colorShape, ColorShape.CIRCLE)
        allowPresets = a.getBoolean(R.styleable.ColorPreference_cpv_allowPresets, true)
        allowCustom = a.getBoolean(R.styleable.ColorPreference_cpv_allowCustom, true)
        showAlphaSlider = a.getBoolean(R.styleable.ColorPreference_cpv_showAlphaSlider, false)
        showColorShades = a.getBoolean(R.styleable.ColorPreference_cpv_showColorShades, true)
        previewSize = a.getInt(R.styleable.ColorPreference_cpv_previewSize, SIZE_NORMAL)
        val presetsResId = a.getResourceId(R.styleable.ColorPreference_cpv_colorPresets, 0)
        dialogTitle = a.getResourceId(R.styleable.ColorPreference_cpv_dialogTitle, R.string.cpv_default_title)
        if (presetsResId != 0) {
            presets = context.resources.getIntArray(presetsResId)
        } else {
            presets = ColorPickerDialog.MATERIAL_COLORS
        }
        if (colorShape == ColorShape.CIRCLE) {
            widgetLayoutResource =
                    if (previewSize == SIZE_LARGE) R.layout.cpv_preference_circle_large else R.layout.cpv_preference_circle
        } else {
            widgetLayoutResource =
                    if (previewSize == SIZE_LARGE) R.layout.cpv_preference_square_large else R.layout.cpv_preference_square
        }
        a.recycle()
    }

    override fun onClick() {
        super.onClick()
        if (onShowDialogListener != null) {
            onShowDialogListener!!.onShowColorPickerDialog(title as String, color)
        } else if (showDialog) {
            val dialog = ColorPickerDialog.newBuilder()
                .setDialogType(dialogType)
                .setDialogTitle(dialogTitle)
                .setColorShape(colorShape)
                .setPresets(presets!!)
                .setAllowPresets(allowPresets)
                .setAllowCustom(allowCustom)
                .setShowAlphaSlider(showAlphaSlider)
                .setShowColorShades(showColorShades)
                .setColor(color)
                .create()
            dialog.setColorPickerDialogListener(this)
            activity.supportFragmentManager
                .beginTransaction()
                .add(dialog, fragmentTag)
                .commitAllowingStateLoss()
        }
    }

    override fun onAttached() {
        super.onAttached()
        if (showDialog) {
            val fragment = activity.supportFragmentManager.findFragmentByTag(fragmentTag) as ColorPickerDialog?
            fragment?.setColorPickerDialogListener(this)
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val preview = holder.itemView.findViewById(R.id.cpv_preference_preview_color_panel) as ColorPanelView
        if (preview != null) {
            preview.color = color
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)
        if (defaultValue is Int) {
            color = (defaultValue as Int?)!!
            persistInt(color)
        } else {
            color = getPersistedInt(-0x1000000)
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInteger(index, Color.BLACK)
    }

    override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
        saveValue(color)
    }

    override fun onDialogDismissed(dialogId: Int) {
        // no-op
    }

    /**
     * Set the new color
     *
     * @param color The newly selected color
     */
    fun saveValue(@ColorInt color: Int) {
        this.color = color
        persistInt(this.color)
        notifyChanged()
        callChangeListener(color)
    }

    /**
     * The listener used for showing the [ColorPickerDialog].
     * Call [.saveValue] after the user chooses a color.
     * If this is set then it is up to you to show the dialog.
     *
     * @param listener The listener to show the dialog
     */
    fun setOnShowDialogListener(listener: OnShowDialogListener) {
        onShowDialogListener = listener
    }

    interface OnShowDialogListener {

        fun onShowColorPickerDialog(title: String, currentColor: Int)
    }

    companion object {

        private val SIZE_NORMAL = 0
        private val SIZE_LARGE = 1
    }
}