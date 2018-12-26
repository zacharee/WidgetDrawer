package tk.zwander.widgetdrawer.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes


class CrashFixExclusionStrategy : ExclusionStrategy {
    private val fieldsToAvoid = setOf(
        "IS_ELASTIC_ENABLED",
        "isElasticEnabled"
    )

    override fun shouldSkipClass(clazz: Class<*>?): Boolean {
        return false
    }

    override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
        val fieldName = fieldAttributes.name

        return fieldsToAvoid.contains(fieldName)
    }
}