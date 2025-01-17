package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun prepMessage(fileName: String, port: Int = SecureRandom().nextInt(9999)): Notification<HmppsDomainEvent> =
    prepMessage(ResourceLoader.message<HmppsDomainEvent>(fileName), port)

fun prepEvent(fileName: String, port: Int = SecureRandom().nextInt(9999)): Notification<HmppsDomainEvent> =
    prepMessage(ResourceLoader.event(fileName), port)

fun prepMessage(message: HmppsDomainEvent, port: Int = SecureRandom().nextInt(9999)): Notification<HmppsDomainEvent> =
    Notification(
        message = message.copy(detailUrl = message.detailUrl?.replace("{wiremock.port}", port.toString())),
        attributes = MessageAttributes(message.eventType)
    )

fun prepNotification(
    notification: Notification<HmppsDomainEvent>,
    port: Int = SecureRandom().nextInt(9999)
): Notification<HmppsDomainEvent> = notification.copy(
    message = notification.message.copy(
        detailUrl = notification.message.detailUrl?.replace(
            "{wiremock.port}",
            port.toString()
        )
    ),
    attributes = notification.attributes
)

fun ZonedDateTime.closeTo(dateTime: ZonedDateTime?, unit: ChronoUnit = ChronoUnit.SECONDS, number: Int = 1): Boolean {
    return dateTime != null && unit.between(
        this.withZoneSameInstant(EuropeLondon),
        dateTime.withZoneSameInstant(EuropeLondon)
    ) <= number
}
