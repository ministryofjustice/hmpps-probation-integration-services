package uk.gov.justice.digital.hmpps.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import uk.gov.justice.digital.hmpps.message.Notification
import kotlin.reflect.KClass

abstract class NotificationConverter<T : Any>(
    val objectMapper: ObjectMapper,
) {
    abstract fun getMessageType(): KClass<T>

    open fun fromMessage(message: String): Notification<T> {
        val stringMessage = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        return Notification(
            message = objectMapper.readValue(stringMessage.message, getMessageType().java),
            attributes = stringMessage.attributes,
        )
    }

    fun toMessage(obj: Notification<*>): String =
        objectMapper.writeValueAsString(
            Notification(
                message = objectMapper.writeValueAsString(obj.message),
                attributes = obj.attributes,
                obj.id,
            ),
        )
}
