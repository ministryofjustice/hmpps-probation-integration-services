package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.ApprovedPremisesService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("approved-premises-and-delius-queue")
class Handler(
    private val telemetryService: TelemetryService,
    private val approvedPremisesService: ApprovedPremisesService,
    override val converter: NotificationConverter<HmppsDomainEvent>,
    @Value("\${event.exception.throw-not-found:true}") private val throwNotFound: Boolean,
) : NotificationHandler<HmppsDomainEvent> {

    @Publish(
        messages = [
            Message(name = "approved-premises/application-submitted"),
            Message(name = "approved-premises/application-assessed"),
            Message(name = "approved-premises/application-withdrawn"),
            Message(name = "approved-premises/booking-made"),
            Message(name = "approved-premises/booking-cancelled"),
            Message(title = "approved-premises.booking.changed", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "approved-premises.person.not-arrived", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "approved-premises.person.arrived", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "approved-premises.person.departed", payload = Schema(HmppsDomainEvent::class)),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
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

                else -> throw IgnorableMessageException("Unexpected Event Type", mapOf("eventType" to event.eventType))
            }
        } catch (ex: HttpStatusCodeException) {
            if (ex.statusCode != HttpStatus.NOT_FOUND || throwNotFound) throw ex
            telemetryService.trackEvent(
                "ApprovedPremisesFailureReport",
                event.telemetryProperties() + ("reason" to "Domain event details not found")
            )
        } catch (ime: IgnorableMessageException) {
            telemetryService.trackEvent(
                "ApprovedPremisesFailureReport",
                event.telemetryProperties() + ime.additionalProperties + ("reason" to ime.message)
            )
        }
    }
}

fun HmppsDomainEvent.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "crn" to crn(),
    detailUrl?.let { "detailUrl" to it }
).toMap()

fun HmppsDomainEvent.crn(): String = personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN")
