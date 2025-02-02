package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getNomsNumberByCrn
import uk.gov.justice.digital.hmpps.integrations.prison.Booking
import uk.gov.justice.digital.hmpps.integrations.prison.Movement
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.DomainEventType.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Component
@Transactional(noRollbackFor = [IgnorableMessageException::class])
@Channel("prison-custody-status-to-delius-queue")
class Handler(
    configContainer: PrisonerMovementConfigs,
    private val featureFlags: FeatureFlags,
    private val telemetryService: TelemetryService,
    private val prisonApiClient: PrisonApiClient,
    private val actionProcessor: ActionProcessor,
    private val personRepository: PersonRepository,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {
    private val configs = configContainer.configs

    @Publish(
        messages = [
            Message(title = "prison-offender-events.prisoner.released", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "prison-offender-events.prisoner.received", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.prison-identifier.added", payload = Schema(HmppsDomainEvent::class)),
            Message(title = "probation-case.prison-identifier.updated", payload = Schema(HmppsDomainEvent::class)),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val message = notification.message
        val eventType = DomainEventType.of(message.eventType)
        val nomsId = message.personReference.findNomsNumber()
            ?: personRepository.getNomsNumberByCrn(requireNotNull(message.personReference.findCrn()))

        try {
            val movement = when (eventType) {
                IdentifierAdded, IdentifierUpdated, PrisonerReceived, PrisonerReleased -> {
                    val booking = prisonApiClient.bookingFromNomsId(nomsId)
                    val movement = prisonApiClient.getLatestMovement(listOf(nomsId)).firstOrNull()
                    movement?.let { booking.prisonerMovement(it) }
                }

                else -> {
                    throw IllegalArgumentException("Unknown event type ${message.eventType}")
                }
            }

            if (movement == null) {
                throw IgnorableMessageException(
                    "NoMovementInNomis", mapOf(
                        "nomsNumber" to nomsId
                    )
                )
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

            val results = if (config.actionNames.isEmpty()) listOf(
                ActionResult.Ignored(
                    "NoActions",
                    movement.telemetryProperties()
                )
            )
            else actionProcessor.processActions(movement, config.actionNames)
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
            telemetryService.trackEvent(e.message, message.telemetryProperties(nomsId) + e.additionalProperties)
        }
    }

    private fun PrisonApiClient.bookingFromNomsId(nomsId: String) = try {
        getBookingByNomsId(nomsId)
    } catch (e: HttpClientErrorException.NotFound) {
        throw IgnorableMessageException("BookingNotFound", mapOf("nomsNumber" to nomsId))
    }.takeIf { it.active || it.movementType == "REL" }
        ?: throw IgnorableMessageException("BookingInactive", mapOf("nomsNumber" to nomsId))
}

fun HmppsDomainEvent.prisonId() = additionalInformation["prisonId"] as String?
fun HmppsDomainEvent.details() = additionalInformation["details"] as String?
fun HmppsDomainEvent.telemetryProperties(nomsId: String) = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "nomsNumber" to nomsId,
    prisonId()?.let { "institution" to it },
    details()?.let { "details" to it }
).toMap()

fun PrisonerMovement?.telemetryProperties(): Map<String, String> = if (this == null) {
    mapOf()
} else {
    listOfNotNull(
        "occurredAt" to occurredAt.toString(),
        "nomsNumber" to nomsId,
        fromPrisonId?.let { "previousInstitution" to it },
        toPrisonId?.let { "institution" to it },
        "reason" to type.name,
        "movementReason" to reason,
        "movementType" to this::class.java.simpleName
    ).toMap()
}

fun Booking.prisonerMovement(movement: Movement): PrisonerMovement {
    val dateTime = ZonedDateTime.of(movement.movementDate, movement.movementTime, EuropeLondon)

    if (reason == null) {
        throw IgnorableMessageException(
            "UnableToCalculateMovementType",
            mapOf(
                "nomsNumber" to personReference,
                "movementType" to movement.movementType,
                "movementReason" to movement.movementReason,
                "inOutStatus" to inOutStatus!!.name,
                "prisonId" to (agencyId ?: "")
            )
        )
    }
    return when (inOutStatus) {
        Booking.InOutStatus.IN -> PrisonerMovement.Received(
            personReference,
            movement.fromAgency,
            movement.toAgency ?: throw IgnorableMessageException("TemporaryAbsenceNoAgency"),
            PrisonerMovement.Type.valueOf(reason),
            movement.movementReason,
            dateTime
        )

        Booking.InOutStatus.OUT -> PrisonerMovement.Released(
            personReference,
            movement.fromAgency ?: throw IgnorableMessageException("TemporaryAbsenceNoAgency"),
            movement.toAgency,
            PrisonerMovement.Type.valueOf(reason),
            movement.movementReason,
            dateTime
        )

        Booking.InOutStatus.TRN -> throw IgnorableMessageException("BeingTransferred")
        else -> throw IgnorableMessageException("NoBookingAvailable")
    }
}
