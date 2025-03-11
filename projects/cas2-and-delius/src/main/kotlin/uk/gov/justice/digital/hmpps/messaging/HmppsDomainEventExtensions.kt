package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent

object HmppsDomainEventExtensions {
    val HmppsDomainEvent.crn get(): String = personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN")
    val HmppsDomainEvent.telemetryProperties get() = mapOf("crn" to crn, "detailUrl" to detailUrl.toString())
}