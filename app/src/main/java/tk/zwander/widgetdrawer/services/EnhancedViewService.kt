package tk.zwander.widgetdrawer.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import tk.zwander.widgetdrawer.utils.*

class EnhancedViewService : AccessibilityService(), EventObserver {
    private val wm by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    override fun onServiceConnected() {
        super.onServiceConnected()

        accessibilityConnected = true

        eventManager.addObserver(this)
        eventManager.sendEvent(Event.AccessibilityConnected)
    }

    override fun onDestroy() {
        super.onDestroy()

        eventManager.sendEvent(Event.AccessibilityDisconnected)
        eventManager.removeObserver(this)
    }

    override fun onInterrupt() {}
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onEvent(event: Event) {
        when (event) {
            Event.OpenDrawerFromAccessibility -> {
                eventManager.sendEvent(Event.ShowDrawer(
                    wm,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                ))
            }
            Event.AddHandleFromAccessibility -> {
                eventManager.sendEvent(Event.ShowHandle(
                    wm,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                ))
            }
            else -> {}
        }
    }
}