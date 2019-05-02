package android.widget

/*
//this will be needed for targeting API 29
//OnClickHandler is a class on API 28 and lower and an interface on API 29
//Interfaces can be implemented through reflection, but classes can't be

class RemoteViews {
    open class OnClickHandler {
        open fun onClickHandler(
            view: View, pendingIntent: PendingIntent,
            fillInIntent: Intent
        ): Boolean {
            return false
        }

        open fun onClickHandler(
            view: View,
            pendingIntent: PendingIntent,
            fillInIntent: Intent,
            windowingMode: Int
        ): Boolean {
            return false
        }

        open fun setEnterAnimationId(enterAnimationId: Int) {}
    }
}
*/