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
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        handleDomainEvent(notification.message)
    }

    private fun handleDomainEvent(hmppsEvent: HmppsDomainEvent) {
        var prisonerMovement: PrisonerMovement? = null
        try {
            when (hmppsEvent.eventType) {
                "probation-case.prison-identifier.added", "probation-case.prison-identifier.updated" -> {
                    val nomsId = hmppsEvent.personReference.findNomsNumber()!!
                    val booking = prisonApiClient.getBookingByNomsId(nomsId).let { b ->
                        prisonApiClient.getBooking(b.id)
                            .takeIf { it.active } ?: throw IgnorableMessageException(
                            "BookingInactive",
                            mapOf("nomsId" to nomsId)
                        )
                    }
                    prisonerMovement = booking.prisonerMovement(hmppsEvent.occurredAt)
                    recordMovement(prisonerMovement)
                }

                "prison-offender-events.prisoner.released" -> {
                    prisonerMovement = hmppsEvent.asReleased()
                    val outcome = releaseService.release(prisonerMovement)
                    telemetryService.trackEvent(outcome.name, hmppsEvent.telemetryProperties())
                }

                "prison-offender-events.prisoner.received" -> {
                    prisonerMovement = hmppsEvent.asReceived()
                    val outcome = recallService.recall(prisonerMovement)
                    telemetryService.trackEvent(outcome.toString(), hmppsEvent.telemetryProperties())
                }

                else -> {
                    throw IllegalArgumentException("Unknown event type ${hmppsEvent.eventType}")
                }
            }
        } catch (e: IgnorableMessageException) {
            telemetryService.trackEvent(
                e.message,
                prisonerMovement.telemetryProperties() + e.additionalProperties
            )
        }
    }

    private fun recordMovement(prisonerMovement: PrisonerMovement) {
        when (prisonerMovement) {
            is PrisonerMovement.Received -> {
                val outcome = recallService.recall(prisonerMovement)
                telemetryService.trackEvent(outcome.name, prisonerMovement.telemetryProperties())
            }

            is PrisonerMovement.Released -> {
                telemetryService.trackEvent("IdentifierAddedForReleasedPrisoner", prisonerMovement.telemetryProperties())
                throw IllegalStateException("NomsNumberAddedAfterRelease")
            }
        }
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

fun PrisonerMovement?.telemetryProperties() = if (this == null) {
    mapOf()
} else {
    listOfNotNull(
        "occurredAt" to occurredAt.toString(),
        "nomsNumber" to nomsId,
        prisonId?.let { "institution" to it },
        "reason" to reason,
        "movementReason" to movementReason,
        "movementType" to this::class.java.simpleName
    ).toMap()
}

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
            mapOf("nomsId" to personReference, "movementType" to movementType, "movementReason" to movementReason)
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
