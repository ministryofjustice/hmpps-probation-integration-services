package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.ProbationMatchRequest
import uk.gov.justice.digital.hmpps.integrations.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.message.Notification
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
    private val probationSearchClient: ProbationSearchClient,
    private val featureFlags: FeatureFlags
) : NotificationHandler<CommonPlatformHearing> {

    @Publish(messages = [Message(title = "COMMON_PLATFORM_HEARING", payload = Schema(CommonPlatformHearing::class))])
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.notificationReceived(notification)

        // Filter hearing message for defendants with a convicted judicial result of Remanded in custody
        val defendants = notification.message.hearing.prosecutionCases
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

            // Insert each defendant as a person record
            val savedEntities = personService.insertPerson(defendant, notification.message.hearing.courtCentre.code)

            val eventName = if (featureFlags.enabled("common-platform-record-creation-toggle")) {
                notifier.caseCreated(savedEntities.person)
                savedEntities.address?.let { notifier.addressCreated(it) }
                "PersonCreated"
            } else {
                "SimulatedPersonCreated"
            }

            telemetryService.trackEvent(
                eventName,
                mapOf(
                    "hearingId" to notification.message.hearing.id,
                    "CRN" to savedEntities.person.crn,
                    "personId" to savedEntities.person.id.toString(),
                    "firstName" to savedEntities.person.forename,
                    "surname" to savedEntities.person.surname,
                    "dob" to savedEntities.person.dateOfBirth.toString(),
                    "genderCode" to savedEntities.person.gender.description,
                    "pnc" to savedEntities.person.pncNumber.toString(),
                    "personManagerId" to savedEntities.personManager.id.toString(),
                    "allocationDate" to savedEntities.personManager.allocationDate.toString(),
                    "allocationReason" to savedEntities.personManager.allocationReason.description,
                    "providerCode" to savedEntities.personManager.provider.code,
                    "teamCode" to savedEntities.personManager.team.code,
                    "staffCode" to savedEntities.personManager.staff.code,
                    "equalityId" to savedEntities.equality.id.toString(),
                    "addressId" to savedEntities.address?.id.toString(),
                    "buildingName" to savedEntities.address?.buildingName.toString(),
                    "addressNumber" to savedEntities.address?.addressNumber.toString(),
                    "streetName" to savedEntities.address?.streetName.toString(),
                    "town" to savedEntities.address?.town.toString(),
                    "district" to savedEntities.address?.district.toString(),
                    "county" to savedEntities.address?.county.toString(),
                    "type" to savedEntities.address?.type?.description.toString(),
                    "status" to savedEntities.address?.status?.description.toString(),
                    "postcode" to savedEntities.address?.postcode.toString(),
                )
            )
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
