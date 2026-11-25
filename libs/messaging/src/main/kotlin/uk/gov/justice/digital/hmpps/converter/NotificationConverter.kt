package uk.gov.justice.digital.hmpps.converter

import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import uk.gov.justice.digital.hmpps.message.Notification
import kotlin.reflect.KClass

abstract class NotificationConverter<T : Any>(
    val objectMapper: ObjectMapper
) {
    abstract fun getMessageType(): KClass<T>

    open fun fromMessage(message: String) = try {
        val stringMessage = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        Notification(
            message = objectMapper.readValue(stringMessage.message, getMessageType().java),
            attributes = stringMessage.attributes
        )
    } catch (e: JacksonException) {
        onMappingError(e)
    }

    fun toMessage(obj: Notification<*>): String = objectMapper.writeValueAsString(
        Notification(
            message = objectMapper.writeValueAsString(obj.message),
            attributes = obj.attributes,
            obj.id
        )
    )

    open fun onMappingError(e: JacksonException): Notification<T>? {
        throw e
    }
}
