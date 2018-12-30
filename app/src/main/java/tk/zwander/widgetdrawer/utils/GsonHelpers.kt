package tk.zwander.widgetdrawer.utils

import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type


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

class UriSerializer : JsonSerializer<Uri> {
    override fun serialize(src: Uri, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }
}

class UriDeserializer : JsonDeserializer<Uri> {
    override fun deserialize(
        src: JsonElement, srcType: Type,
        context: JsonDeserializationContext
    ): Uri? {
        return try {
            Uri.parse(src.asString)
        } catch (e: Exception) {
            null
        }
    }
}