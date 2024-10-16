package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.client.ProbationMatchRequest
import uk.gov.justice.digital.hmpps.integrations.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.entity.repository.ReferenceDataRepository
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
    private val referenceDataRepository: ReferenceDataRepository,
    private val probationSearchClient: ProbationSearchClient
) : NotificationHandler<CommonPlatformHearing> {

    @Publish(messages = [Message(title = "COMMON_PLATFORM_HEARING", payload = Schema(CommonPlatformHearing::class))])
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.notificationReceived(notification)

        val defendant = notification.message.hearing.prosecutionCases.firstOrNull()?.defendants?.firstOrNull()
            ?: throw IllegalArgumentException("No prosecution cases found")
        val courtCode = notification.message.hearing.courtCentre.code.toDeliusCourtCode()

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
            log.debug("Matching offender(s) found for: {}", matchRequest)
            return
        }
        personService.insertPerson(defendant.toPerson(), courtCode)
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun Defendant.toPerson(): Person {
        val personDetails = personDefendant?.personDetails ?: throw IllegalArgumentException("No person found")
        val genderCode = personDetails.gender.toDeliusGender()

        return Person(
            id = null,
            crn = personService.generateCrn(),
            croNumber = this.croNumber,
            pncNumber = this.pncId,
            forename = personDetails.firstName,
            secondName = personDetails.middleName,
            surname = personDetails.lastName,
            dateOfBirth = personDetails.dateOfBirth,
            gender = referenceDataRepository.findByCode(genderCode),
            softDeleted = false
        )
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

    fun String.toDeliusGender() = ReferenceData.GenderCode.entries.find { it.commonPlatformValue == this }?.deliusValue
        ?: throw IllegalStateException("Gender not found: $this")

    fun String.toDeliusCourtCode() = Court.CourtCode.entries.find { it.commonPlatformValue == this }?.deliusValue
        ?: throw IllegalStateException("Court not found: $this")
}





