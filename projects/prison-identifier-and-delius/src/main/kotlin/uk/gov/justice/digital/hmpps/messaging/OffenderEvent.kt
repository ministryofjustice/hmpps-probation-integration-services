package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Message

@Message
data class OffenderEvent(
    val crn: String,
)
