package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Primary
@Component
class Converter(objectMapper: ObjectMapper, private val telemetryService: TelemetryService) :
    NotificationConverter<CommonPlatformHearing>(objectMapper) {
    override fun getMessageType() = CommonPlatformHearing::class

    override fun onMappingError(e: JsonMappingException): Notification<CommonPlatformHearing>? {
        // TODO remove this once we are able to handle large messages that are stored in S3
        telemetryService.trackException(e)
        return null
    }
}