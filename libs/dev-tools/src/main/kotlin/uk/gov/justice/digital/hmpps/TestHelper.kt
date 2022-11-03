package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import java.time.Duration
import java.time.Instant.now
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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

fun waitUntil(timeout: Duration = Duration.ofSeconds(5), interval: Long = 500, block: () -> Boolean) {
    val end = now().plus(timeout)

    while (!block() && now().isBefore(end)) {
        TimeUnit.MILLISECONDS.sleep(interval)
    }
    if (now().isAfter(end)) {
        throw TimeoutException("timeout reached ${timeout}")
    }
}
