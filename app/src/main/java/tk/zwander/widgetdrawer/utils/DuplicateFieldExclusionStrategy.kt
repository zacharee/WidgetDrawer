package tk.zwander.widgetdrawer.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes


class DuplicateFieldExclusionStrategy : ExclusionStrategy {
    private val fields = HashSet<String>()

    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
        return false
    }

    override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
        val fieldName = fieldAttributes.name

        return if (fields.contains(fieldName)) true
        else {
            fields.add(fieldName)
            false
        }
    }
}