package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.IdentifierAdded
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.IdentifierUpdated
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.PrisonerReceived
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.PrisonerReleased
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Component
class Handler(
    configContainer: PrisonerMovementConfigs,
    private val featureFlags: FeatureFlags,
    private val telemetryService: TelemetryService,
    private val prisonApiClient: PrisonApiClient,
    private val actionProcessor: ActionProcessor,
    override val converter: NotificationConverter<HmppsDomainEvent>
) : NotificationHandler<HmppsDomainEvent> {
    private val configs = configContainer.configs

    override fun handle(notification: Notification<HmppsDomainEvent>) {
        val message = notification.message
        val eventType = DomainEventType.of(message.eventType)
        try {
            val movement = when (eventType) {
                IdentifierAdded, IdentifierUpdated ->
                    prisonApiClient.bookingFromNomsId(message.personReference.findNomsNumber()!!)
                        .prisonerMovement(message.occurredAt)

                PrisonerReceived -> message.asReceived()

                PrisonerReleased -> message.asReleased()

                else -> {
                    throw IllegalArgumentException("Unknown event type ${message.eventType}")
                }
            }
            val config = configs.firstOrNull { it.validFor(movement.type, movement.reason) }
                ?: throw IgnorableMessageException(
                    "NoConfigForMovement",
                    mapOf(
                        "nomsNumber" to movement.nomsId,
                        "movementType" to movement.type.name,
                        "movementReason" to movement.reason
                    )
                )

            if (config.featureFlag != null && !featureFlags.enabled(config.featureFlag)) {
                if (config.reasonOverride == null) {
                    return telemetryService.trackEvent(
                        "FeatureFlagNotActive",
                        movement.telemetryProperties() + ("featureFlag" to config.featureFlag)
                    )
                } else {
                    movement.reasonOverride = config.reasonOverride
                }
            }

            val results = actionProcessor.processActions(movement, config.actionNames)
            val failure = results.firstOrNull { it is ActionResult.Failure } as ActionResult.Failure?
            if (failure == null) {
                results.forEach {
                    when (it) {
                        is ActionResult.Success -> telemetryService.trackEvent(it.type.name, it.properties)
                        is ActionResult.Ignored -> telemetryService.trackEvent(it.reason, it.properties)
                        else -> throw IllegalArgumentException("Unexpected Action Result: $it")
                    }
                }
            } else {
                throw failure.exception
            }
        } catch (e: IgnorableMessageException) {
            telemetryService.trackEvent(
                e.message,
                message.telemetryProperties() + e.additionalProperties
            )
        }
    }

    private fun PrisonApiClient.bookingFromNomsId(nomsId: String) = getBookingByNomsId(nomsId).let { b ->
        getBooking(b.id).takeIf { it.active }
            ?: throw IgnorableMessageException("BookingInactive", mapOf("nomsNumber" to nomsId))
    }
}

fun AdditionalInformation.prisonId() = this["prisonId"] as String?
fun AdditionalInformation.reason() = this["reason"] as String
fun AdditionalInformation.movementReason() = this["nomisMovementReasonCode"] as String
fun AdditionalInformation.details() = this["details"] as String?
fun HmppsDomainEvent.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "nomsNumber" to personReference.findNomsNumber()!!,
    additionalInformation.prisonId()?.let { "institution" to it },
    additionalInformation.details()?.let { "details" to it }
).toMap()

fun PrisonerMovement?.telemetryProperties() = if (this == null) {
    mapOf()
} else {
    listOfNotNull(
        "occurredAt" to occurredAt.toString(),
        "nomsNumber" to nomsId,
        prisonId?.let { "institution" to it },
        "reason" to type.name,
        "movementReason" to reason,
        "movementType" to this::class.java.simpleName
    ).toMap()
}

fun HmppsDomainEvent.asReceived() = PrisonerMovement.Received(
    personReference.findNomsNumber()!!,
    additionalInformation.prisonId()!!,
    PrisonerMovement.Type.valueOf(additionalInformation.reason()),
    additionalInformation.movementReason(),
    occurredAt
)

fun HmppsDomainEvent.asReleased() = PrisonerMovement.Released(
    personReference.findNomsNumber()!!,
    additionalInformation.prisonId(),
    PrisonerMovement.Type.valueOf(additionalInformation.reason()),
    additionalInformation.movementReason(),
    occurredAt
)

fun Booking.prisonerMovement(dateTime: ZonedDateTime): PrisonerMovement {
    if (reason == null) {
        throw IgnorableMessageException(
            "UnableToCalculateMovementType",
            mapOf("nomsNumber" to personReference, "movementType" to movementType, "movementReason" to movementReason)
        )
    }
    return when (inOutStatus) {
        Booking.InOutStatus.IN -> PrisonerMovement.Received(
            personReference,
            agencyId,
            PrisonerMovement.Type.valueOf(reason),
            movementReason,
            dateTime
        )

        Booking.InOutStatus.OUT -> PrisonerMovement.Released(
            personReference,
            null,
            PrisonerMovement.Type.valueOf(reason),
            movementReason,
            dateTime
        )
    }
}
