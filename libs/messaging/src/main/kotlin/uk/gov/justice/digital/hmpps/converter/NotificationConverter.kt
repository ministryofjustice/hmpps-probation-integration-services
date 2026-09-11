package uk.gov.justice.digital.hmpps.converter

import software.amazon.payloadoffloading.S3BackedPayloadStore
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import uk.gov.justice.digital.hmpps.message.Notification
import kotlin.reflect.KClass

private fun String.isS3Pointer() =
    trimStart().startsWith("[\"software.amazon.payloadoffloading.PayloadS3Pointer\"")

abstract class NotificationConverter<T : Any>(
    val objectMapper: ObjectMapper,
    private val payloadStore: S3BackedPayloadStore? = null
) {
    abstract fun getMessageType(): KClass<T>

    open fun fromMessage(message: String) = try {
        val stringMessage = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        val resolvedPayload = if (payloadStore != null && stringMessage.message.isS3Pointer()) {
            payloadStore.getOriginalPayload(stringMessage.message)
        } else {
            stringMessage.message
        }
        Notification(
            message = objectMapper.readValue(resolvedPayload, getMessageType().java),
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
