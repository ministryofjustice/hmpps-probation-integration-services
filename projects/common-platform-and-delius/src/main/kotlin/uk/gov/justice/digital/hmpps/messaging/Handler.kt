package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.client.ProbationMatchRequest
import uk.gov.justice.digital.hmpps.integrations.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.EventService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("common-platform-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<CommonPlatformHearing>,
    private val notifier: Notifier,
    private val telemetryService: TelemetryService,
    private val personService: PersonService,
    private val eventService: EventService,
    private val probationSearchClient: ProbationSearchClient,
    private val courtAppearanceRepository: CourtAppearanceRepository
) : NotificationHandler<CommonPlatformHearing> {

    @Publish(messages = [Message(title = "COMMON_PLATFORM_HEARING", payload = Schema(CommonPlatformHearing::class))])
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.notificationReceived(notification)

        val hearing = notification.message.hearing
        val courtCode = hearing.courtCentre.code

        val defendants = hearing.prosecutionCases
            .flatMap { it.defendants }
            .filter { defendant ->
                defendant.offences.any { offence ->
                    offence.judicialResults?.any { judicialResult ->
                        judicialResult.isConvictedResult == true && judicialResult.label == "Remanded in custody"
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
                return@forEach
            }

            // Insert each defendant as a person record, send relevant SNS messages
            val savedEntities = personService.insertPerson(defendant, courtCode)
            notifier.caseCreated(savedEntities.person)
            savedEntities.address?.let { notifier.addressCreated(it) }

            telemetryService.trackEvent(
                "PersonCreated", mapOf(
                    "hearingId" to hearing.id,
                    "CRN" to savedEntities.person.crn,
                    "personId" to savedEntities.person.id.toString(),
                    "personManagerId" to savedEntities.personManager.id.toString(),
                    "equalityId" to savedEntities.equality.id.toString(),
                    "addressId" to savedEntities.address?.id.toString()
                )
            )

            val remandedOffences = defendant.offences.filter { offence ->
                offence.judicialResults?.any { it.label == "Remanded in custody" } == true
            }

            val mainOffence = remandedOffences.firstOrNull()
                ?: return@forEach

            val caseUrn = hearing.prosecutionCases
                .find { it.defendants.contains(defendant) }
                ?.prosecutionCaseIdentifier?.caseURN ?: return@forEach

            val existingCourtAppearance = courtAppearanceRepository.findLatestByCaseUrn(caseUrn)

            if (existingCourtAppearance != null) {
                eventService.insertCourtAppearance(
                    existingCourtAppearance.event,
                    courtCode,
                    hearing.hearingDays.first().sittingDay,
                    caseUrn
                )
            } else {
                eventService.insertEvent(
                    mainOffence,
                    savedEntities.person,
                    courtCode,
                    hearing.hearingDays.first().sittingDay,
                    caseUrn
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

