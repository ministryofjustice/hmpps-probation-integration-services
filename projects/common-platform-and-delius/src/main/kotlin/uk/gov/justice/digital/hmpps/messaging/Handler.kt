package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
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
    private val telemetryService: TelemetryService,
    private val personService: PersonService,
    private val probationSearchClient: ProbationSearchClient
) : NotificationHandler<CommonPlatformHearing> {

    @Publish(messages = [Message(title = "COMMON_PLATFORM_HEARING", payload = Schema(CommonPlatformHearing::class))])
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.notificationReceived(notification)

        val defendants = notification.message.hearing.prosecutionCases
            .flatMap { it.defendants }
            .ifEmpty { throw IllegalArgumentException("No defendants found") }

        val courtCode = notification.message.hearing.courtCentre.code

        val dateOfBirth = notification.message.hearing.prosecutionCases
            .firstOrNull()?.defendants?.firstOrNull()
            ?.personDefendant?.personDetails?.dateOfBirth
            ?: throw IllegalArgumentException("Date of birth not found in message")

        // Under 10 years old validation
        dateOfBirth.let {
            val age = Period.between(it, LocalDate.now()).years
            require(age > 10) {
                "Date of birth would indicate person is under ten years old: $it"
            }
        }

        val matchRequest = notification.message.toProbationMatchRequest()
        val matchedPersonResponse = probationSearchClient.match(matchRequest)

        if (matchedPersonResponse.matches.isNotEmpty()) {
            return
        }
        defendants.forEach { defendant ->
            // Insert each defendant as a person record
            val savedPerson = personService.insertPerson(defendant, courtCode)

            telemetryService.trackEvent(
                "PersonCreated", mapOf(
                    "CRN" to savedPerson.crn,
                    "personId" to savedPerson.id.toString(),
                    "hearingId" to notification.message.hearing.id
                )
            )
        }
    }

    fun CommonPlatformHearing.toProbationMatchRequest(): ProbationMatchRequest {
        val defendant = this.hearing.prosecutionCases.firstOrNull()?.defendants?.firstOrNull()
        val personDetails =
            defendant?.personDefendant?.personDetails ?: throw IllegalArgumentException("Person details are required")
        return ProbationMatchRequest(
            firstName = personDetails.firstName,
            surname = personDetails.lastName,
            dateOfBirth = personDetails.dateOfBirth,
            pncNumber = defendant.pncId,
            croNumber = defendant.croNumber
        )
    }
}





