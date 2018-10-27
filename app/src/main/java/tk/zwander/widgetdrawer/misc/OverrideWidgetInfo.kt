package tk.zwander.widgetdrawer.misc

data class OverrideWidgetInfo(
    var id: Int,
    var forcedHeight: Int,
    var isFullWidth: Boolean,
    var isSelected: Boolean = false
)