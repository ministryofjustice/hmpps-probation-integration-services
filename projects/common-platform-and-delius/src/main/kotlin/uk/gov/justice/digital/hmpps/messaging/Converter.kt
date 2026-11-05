package uk.gov.justice.digital.hmpps.messaging

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.converter.NotificationConverter

@Primary
@Component
class Converter(objectMapper: ObjectMapper) :
    NotificationConverter<CommonPlatformHearing>(objectMapper) {
    override fun getMessageType() = CommonPlatformHearing::class
}