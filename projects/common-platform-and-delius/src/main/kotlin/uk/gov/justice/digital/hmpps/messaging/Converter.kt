package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification

@Primary
@Component
class Converter(objectMapper: ObjectMapper) : NotificationConverter<CommonPlatformHearing>(objectMapper) {
    override fun getMessageType() = CommonPlatformHearing::class

    override fun fromMessage(message: String): Notification<CommonPlatformHearing> {
        val stringMessage = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        return Notification(
            message = objectMapper.readValue(stringMessage.message, CommonPlatformHearing::class.java),
            attributes = stringMessage.attributes
        )
    }
}