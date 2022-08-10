package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.message.HmppsEvent
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun prepMessage(fileName: String, port: Int): HmppsEvent {
    val allocationEvent = ResourceLoader.allocationMessage(fileName)
    return allocationEvent.copy(
        detailUrl = allocationEvent.detailUrl.replace(
            "{wiremock.port}",
            port.toString()
        )
    )
}

fun ZonedDateTime.closeTo(dateTime: ZonedDateTime?, unit: ChronoUnit = ChronoUnit.SECONDS, number: Int = 1): Boolean {
    return dateTime != null && unit.between(
        this.withZoneSameInstant(EuropeLondon),
        dateTime.withZoneSameInstant(EuropeLondon)
    ) <= number
}
