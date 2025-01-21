package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Message

@Message
data class OffenderEvent(
    val crn: String,
)
