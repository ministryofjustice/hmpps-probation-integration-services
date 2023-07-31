package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.recall.RecallService
import uk.gov.justice.digital.hmpps.integrations.delius.release.ReleaseService
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.time.ZonedDateTime

@Component
class Handler(
    private val telemetryService: TelemetryService,
    private val releaseService: ReleaseService,
    private val recallService: RecallService,
    private val prisonApiClient: PrisonApiClient,
    override val converter: NotificationConverter<Any>
) : NotificationHandler<Any> {
    override fun handle(notification: Notification<Any>) {
        telemetryService.notificationReceived(notification)
        when (val message = notification.message) {
            is HmppsDomainEvent -> handleDomainEvent(message)
            is CustodialStatusChanged -> handleCsc(message)
        }
    }

    private fun handleDomainEvent(hmppsEvent: HmppsDomainEvent) =
        try {
            when (hmppsEvent.eventType) {
                "prison-offender-events.prisoner.released" -> {
                    val outcome = releaseService.release(hmppsEvent.asReleased())
                    telemetryService.trackEvent(outcome.name, hmppsEvent.telemetryProperties())
                }

                "prison-offender-events.prisoner.received" -> {
                    val outcome = recallService.recall(hmppsEvent.asReceived())
                    telemetryService.trackEvent(outcome.toString(), hmppsEvent.telemetryProperties())
                }

                else -> throw IllegalArgumentException("Unknown event type ${hmppsEvent.eventType}")
            }
        } catch (e: IgnorableMessageException) {
            telemetryService.trackEvent(e.message, hmppsEvent.telemetryProperties() + e.additionalProperties)
        }

    private fun handleCsc(csc: CustodialStatusChanged) = try {
        if (csc.imprisonmentStatusSeq != null && csc.imprisonmentStatusSeq > 0) {
            throw IgnorableMessageException(
                "DuplicateImprisonmentStatus",
                csc.telemetryProperties()
            )
        }
        val booking = prisonApiClient.getBooking(csc.bookingId).takeIf { it.active }
            ?: throw IgnorableMessageException("BookingInactive")
        val prisonerMovement = booking.prisonerMovement(csc.eventDatetime)
        when (booking.inOutStatus) {
            Booking.InOutStatus.IN -> {
                val outcome = recallService.recall(prisonerMovement)
                telemetryService.trackEvent(outcome.name, csc.telemetryProperties() + prisonerMovement.telemetryProperties())
            }

            Booking.InOutStatus.OUT -> {
                val outcome = releaseService.release(prisonerMovement)
                telemetryService.trackEvent(outcome.name, csc.telemetryProperties() + prisonerMovement.telemetryProperties())
            }
        }
    } catch (e: IgnorableMessageException) {
        telemetryService.trackEvent(
            e.message,
            csc.telemetryProperties() + e.additionalProperties
        )
    }
}

fun AdditionalInformation.nomsNumber() = this["nomsNumber"] as String
fun AdditionalInformation.prisonId() = this["prisonId"] as String?
fun AdditionalInformation.reason() = this["reason"] as String
fun AdditionalInformation.movementReason() = this["nomisMovementReasonCode"] as String
fun AdditionalInformation.details() = this["details"] as String?
fun HmppsDomainEvent.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "nomsNumber" to additionalInformation.nomsNumber(),
    additionalInformation.prisonId()?.let { "institution" to it },
    "reason" to additionalInformation.reason(),
    "nomisMovementReasonCode" to additionalInformation.movementReason(),
    additionalInformation.details()?.let { "details" to it }
).toMap()

fun CustodialStatusChanged.telemetryProperties() = listOfNotNull(
    "bookingId" to bookingId.toString(),
    movementSeq?.let { "movementSeq" to it.toString() },
    imprisonmentStatusSeq?.let { "imprisonmentStatusSeq" to it.toString() }
).toMap()

fun PrisonerMovement.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "nomsNumber" to nomsId,
    prisonId?.let { "institution" to it },
    "reason" to reason,
    "movementReasonCode" to movementReason,
    "movementType" to this::class.java.simpleName
).toMap()

fun HmppsDomainEvent.asReceived() = PrisonerMovement.Received(
    additionalInformation.nomsNumber(),
    additionalInformation.prisonId()!!,
    additionalInformation.reason(),
    additionalInformation.movementReason(),
    occurredAt
)

fun HmppsDomainEvent.asReleased() = PrisonerMovement.Released(
    additionalInformation.nomsNumber(),
    additionalInformation.prisonId()!!,
    additionalInformation.reason(),
    additionalInformation.movementReason(),
    occurredAt
)

fun Booking.prisonerMovement(dateTime: ZonedDateTime): PrisonerMovement {
    if (reason == null) {
        throw IgnorableMessageException(
            "UnableToCalculateMovementType",
            mapOf("movementType" to movementType, "movementReason" to movementReason)
        )
    }
    return when (inOutStatus) {
        Booking.InOutStatus.IN -> PrisonerMovement.Received(
            personReference,
            agencyId,
            reason!!,
            movementReason,
            dateTime
        )

        Booking.InOutStatus.OUT -> PrisonerMovement.Released(
            personReference,
            agencyId,
            reason!!,
            movementReason,
            dateTime
        )
    }
}
