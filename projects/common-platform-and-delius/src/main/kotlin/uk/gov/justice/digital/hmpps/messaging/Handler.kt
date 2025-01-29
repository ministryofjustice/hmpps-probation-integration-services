package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.ProbationMatchRequest
import uk.gov.justice.digital.hmpps.integrations.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EventRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.EventService
import uk.gov.justice.digital.hmpps.service.InsertEventResult
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.Period

@Component
@Channel("common-platform-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<CommonPlatformHearing>,
    private val notifier: Notifier,
    private val telemetryService: TelemetryService,
    private val personService: PersonService,
    private val probationSearchClient: ProbationSearchClient,
    private val featureFlags: FeatureFlags,
    private val eventService: EventService,
    private val eventRepository: EventRepository
) : NotificationHandler<CommonPlatformHearing> {

    @Publish(messages = [Message(title = "COMMON_PLATFORM_HEARING", payload = Schema(CommonPlatformHearing::class))])
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.notificationReceived(notification)

        // Filter hearing message for defendants containing a judicial result label of 'Remanded in custody'
        val defendants = notification.message.hearing.prosecutionCases.flatMap { it.defendants }.filter { defendant ->
            defendant.offences.any { offence ->
                offence.judicialResults?.any { judicialResult ->
                    judicialResult.label == "Remanded in custody"
                } ?: false
            }
        }
        if (defendants.isEmpty()) {
            return
        }

        defendants.forEach { defendant ->

            val matchRequest = defendant.toProbationMatchRequest() ?: return@forEach

            val matchedPersonResponse = probationSearchClient.match(matchRequest)

            if (matchedPersonResponse.matches.isNotEmpty()) {
                telemetryService.trackEvent(
                    "ProbationSearchMatchDetected", mapOf(
                        "hearingId" to notification.message.hearing.id,
                        "defendantId" to defendant.id,
                        "crns" to matchedPersonResponse.matches.joinToString(", ") { it.offender.otherIds.crn })
                )
                return@forEach
            }

            val dateOfBirth = defendant.personDefendant?.personDetails?.dateOfBirth
                ?: throw IllegalArgumentException("Date of birth not found in message")

            // Under 10 years old validation
            dateOfBirth.let {
                val age = Period.between(it, LocalDate.now()).years
                require(age > 10) {
                    "Date of birth would indicate person is under ten years old: $it"
                }
            }

            if (featureFlags.enabled("common-platform-record-creation-toggle")) {
                // Insert each defendant as a person record
                val savedEntities = personService.insertPerson(defendant, notification.message.hearing.courtCentre.code)

                notifier.caseCreated(savedEntities.person)
                savedEntities.address?.let { notifier.addressCreated(it) }

                telemetryService.trackEvent(
                    "PersonCreated", mapOf(
                        "hearingId" to notification.message.hearing.id,
                        "defendantId" to defendant.id,
                        "CRN" to savedEntities.person.crn,
                        "personId" to savedEntities.person.id.toString(),
                        "personManagerId" to savedEntities.personManager.id.toString(),
                        "equalityId" to savedEntities.equality.id.toString(),
                        "addressId" to savedEntities.address?.id.toString(),
                    )
                )

                val remandedOffences = defendant.offences.filter { offence ->
                    offence.judicialResults?.any { it.label == "Remanded in custody" } == true
                }

                // TODO: Currently using the first offence, We will need to identify the main offence
                val mainOffence = remandedOffences.firstOrNull() ?: return@forEach

                val caseUrn =
                    notification.message.hearing.prosecutionCases.find { it.defendants.contains(defendant) }?.prosecutionCaseIdentifier?.caseURN
                        ?: return@forEach

                // Event logic
                val savedEventEntities: InsertEventResult?
                val existingCaseUrnEvent = eventRepository.findEventByCaseUrnAndCrn(caseUrn, savedEntities.person.crn)
                val otherActiveEvents =
                    eventRepository.findActiveEventsExcludingCaseUrn(caseUrn, savedEntities.person.crn)

                // If an existing event with case urn exists, update it.
                // Unless other active events exist on the person, in which case do nothing
                // If no existing events are found and no case urn event is found then create a new event
                if (existingCaseUrnEvent != null) {
                    if (!otherActiveEvents.isNullOrEmpty()) {
                        telemetryService.trackEvent(
                            "EventUpdateSkipped", mapOf(
                                "hearingId" to notification.message.hearing.id,
                                "existingEventIdsFound" to otherActiveEvents.joinToString(",") { it.id.toString() })
                        )
                        return@forEach
                    } else {
                        // TODO: Implement update event logic here
                        // savedEventEntities = eventService.updateEvent()
                        telemetryService.trackEvent(
                            "SimulatedUpdateEvent", mapOf("hearingId" to notification.message.hearing.id)
                        )
                    }
                } else {
                    if (otherActiveEvents.isNullOrEmpty()) {
                        savedEventEntities = eventService.insertEvent(
                            mainOffence,
                            savedEntities.person,
                            notification.message.hearing.courtCentre.code,
                            notification.message.hearing.hearingDays.first().sittingDay,
                            caseUrn,
                            notification.message.hearing.id
                        )
                        telemetryService.trackEvent(
                            "EventCreated", mapOf(
                                "hearingId" to notification.message.hearing.id,
                                "eventId" to savedEventEntities.event.id.toString(),
                                "eventNumber" to savedEventEntities.event.number,
                                "CRN" to savedEventEntities.event.person.crn,
                                "personId" to savedEventEntities.event.person.id.toString(),
                                "orderManagerId" to savedEventEntities.orderManager.id.toString(),
                                "mainOffenceId" to savedEventEntities.mainOffence.id.toString(),
                                "courtAppearanceId" to savedEventEntities.courtAppearance.id.toString(),
                                "contactId" to savedEventEntities.contact.id.toString()
                            )
                        )
                    } else {
                        telemetryService.trackEvent(
                            "EventCreatedSkipped", mapOf(
                                "hearingId" to notification.message.hearing.id,
                                "existingActiveEventIds" to otherActiveEvents.joinToString(",") { it.id.toString() },
                            )
                        )
                        return@forEach
                    }
                }
            } else {
                telemetryService.trackEvent(
                    "SimulatedPersonCreated", mapOf(
                        "hearingId" to notification.message.hearing.id, "defendantId" to defendant.id
                    )
                )
            }
        }
    }

    fun Defendant.toProbationMatchRequest(): ProbationMatchRequest? {
        val personDetails = this.personDefendant?.personDetails

        val firstName = personDetails?.firstName
        val lastName = personDetails?.lastName
        val dateOfBirth = personDetails?.dateOfBirth

        // Return null if any required fields are missing
        if (firstName == null || lastName == null || dateOfBirth == null) {
            return null
        }

        return ProbationMatchRequest(
            firstName = firstName,
            surname = lastName,
            dateOfBirth = dateOfBirth,
            pncNumber = this.pncId,
            croNumber = this.croNumber
        )
    }
}
