package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.ApprovedPremisesService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val approvedPremisesService: ApprovedPremisesService,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        val event = notification.message
        try {
            when (event.eventType) {
                "approved-premises.application.submitted" -> {
                    approvedPremisesService.applicationSubmitted(event)
                    telemetryService.trackEvent("ApplicationSubmitted", event.telemetryProperties())
                }

                "approved-premises.application.assessed" -> {
                    approvedPremisesService.applicationAssessed(event)
                    telemetryService.trackEvent("ApplicationAssessed", event.telemetryProperties())
                }

                "approved-premises.application.withdrawn" -> {
                    approvedPremisesService.applicationWithdrawn(event)
                    telemetryService.trackEvent("ApplicationWithdrawn", event.telemetryProperties())
                }

                "approved-premises.booking.made" -> {
                    approvedPremisesService.bookingMade(event)
                    telemetryService.trackEvent("BookingMade", event.telemetryProperties())
                }

                "approved-premises.booking.changed" -> {
                    approvedPremisesService.bookingChanged(event)
                    telemetryService.trackEvent("BookingChanged", event.telemetryProperties())
                }

                "approved-premises.booking.cancelled" -> {
                    approvedPremisesService.bookingCancelled(event)
                    telemetryService.trackEvent("BookingCancelled", event.telemetryProperties())
                }

                "approved-premises.person.not-arrived" -> {
                    approvedPremisesService.personNotArrived(event)
                    telemetryService.trackEvent("PersonNotArrived", event.telemetryProperties())
                }

                "approved-premises.person.arrived" -> {
                    approvedPremisesService.personArrived(event)
                    telemetryService.trackEvent("PersonArrived", event.telemetryProperties())
                }

                "approved-premises.person.departed" -> {
                    approvedPremisesService.personDeparted(event)
                    telemetryService.trackEvent("PersonDeparted", event.telemetryProperties())
                }

                else -> throw IgnorableMessageException("UnexpectedEventType", mapOf("eventType" to event.eventType))
            }
        } catch (ime: IgnorableMessageException) {
            telemetryService.trackEvent(ime.message, event.telemetryProperties() + ime.additionalProperties)
        }
    }
}

fun HmppsDomainEvent.telemetryProperties() = mapOf(
    "occurredAt" to occurredAt.toString(),
    "crn" to crn()
)

fun HmppsDomainEvent.crn(): String = personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN")
fun HmppsDomainEvent.url(): URI = URI.create(detailUrl ?: throw IllegalArgumentException("Missing detail url"))
