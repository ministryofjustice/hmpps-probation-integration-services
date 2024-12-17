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
                telemetryService.trackEvent(
                    "ProbationSearchMatchDetected",
                    mapOf(
                        "hearingId" to notification.message.hearing.id,
                        "defendantId" to defendant.id,
                        "crns" to matchedPersonResponse.matches.joinToString(", ") { it.offender.otherIds.crn }
                    )
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
                    "PersonCreated",
                    mapOf(
                        "hearingId" to notification.message.hearing.id,
                        "defendantId" to defendant.id,
                        "CRN" to savedEntities.person.crn,
                        "personId" to savedEntities.person.id.toString(),
                        "personManagerId" to savedEntities.personManager.id.toString(),
                        "equalityId" to savedEntities.equality.id.toString(),
                        "addressId" to savedEntities.address?.id.toString(),
                    )
                )
            } else {
                telemetryService.trackEvent(
                    "SimulatedPersonCreated",
                    mapOf(
                        "hearingId" to notification.message.hearing.id,
                        "defendantId" to defendant.id
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
