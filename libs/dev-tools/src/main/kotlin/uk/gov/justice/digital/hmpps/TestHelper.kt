package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun prepMessage(fileName: String, port: Int): Notification<HmppsDomainEvent> {
    val hmppsEvent = ResourceLoader.message<HmppsDomainEvent>(fileName)
    return Notification(
        message = hmppsEvent.copy(
            detailUrl = hmppsEvent.detailUrl?.replace("{wiremock.port}", port.toString())
        )
    )
}

fun ZonedDateTime.closeTo(dateTime: ZonedDateTime?, unit: ChronoUnit = ChronoUnit.SECONDS, number: Int = 1): Boolean {
    return dateTime != null && unit.between(
        this.withZoneSameInstant(EuropeLondon),
        dateTime.withZoneSameInstant(EuropeLondon)
    ) <= number
}
