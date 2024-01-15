package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.net.URI

object HmppsDomainEventExtensions {
    val HmppsDomainEvent.crn get(): String = personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN")
    val HmppsDomainEvent.url get(): URI = URI.create(detailUrl ?: throw IllegalArgumentException("Missing detail url"))
    val HmppsDomainEvent.telemetryProperties get() = mapOf("crn" to crn, "detailUrl" to detailUrl.toString())
}