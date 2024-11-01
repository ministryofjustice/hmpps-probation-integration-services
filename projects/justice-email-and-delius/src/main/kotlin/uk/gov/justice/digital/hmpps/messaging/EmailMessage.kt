package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Message
import java.time.ZonedDateTime

@Message
data class EmailMessage(
    val id: String,
    val subject: String,
    val bodyContent: String,
    val fromEmailAddress: String,
    val receivedDateTime: ZonedDateTime,
)
