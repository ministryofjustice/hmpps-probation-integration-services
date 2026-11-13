package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import io.sentry.Sentry
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.dto.InsertPersonResult
import uk.gov.justice.digital.hmpps.dto.InsertRemandDTO
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.*
import uk.gov.justice.digital.hmpps.integrations.client.Address
import uk.gov.justice.digital.hmpps.integrations.client.ContactDetails
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.retry.retry
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.service.RemandService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.YEARS

@Primary
@Component
@Channel("common-platform-and-delius-fifo-queue")
class Handler(
    override val converter: NotificationConverter<CommonPlatformHearing>,
    private val telemetryService: TelemetryService,
    private val personService: PersonService,
    private val featureFlags: FeatureFlags,
    private val remandService: RemandService,
    private val offenceService: OffenceService,
    private val corePersonClient: CorePersonClient,
    private val notifier: Notifier,
) : NotificationHandler<CommonPlatformHearing> {

    @Publish(messages = [Message(title = "COMMON_PLATFORM_HEARING", payload = Schema(CommonPlatformHearing::class))])
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.notificationReceived(notification)

        // Filter hearing message for defendants containing a judicial result label of 'Remanded in custody'
        val defendants = notification.message.hearing.prosecutionCases.flatMap { it.defendants }.filter { defendant ->
            defendant.offences.mapNotNull { it.judicialResults }.flatten().any { it.label == "Remanded in custody" }
        }.ifEmpty { return }

        defendants.forEach { defendant ->
            val matchedPersonStatus = corePersonClient.findMatchStatusByDefendantId(defendant.id)
            var telemetryProperties = matchedPersonStatus.telemetryProperties + notification.telemetryProperties +
                mapOf("defendantId" to defendant.id)

            // Defendant is matched with existing record in core person service
            if (matchedPersonStatus.matchStatus == CorePersonRecordMatchStatus.MATCH) {
                telemetryService.trackEvent("PersonAlreadyExists", telemetryProperties)
                return@forEach
            }

            val caseUrn = notification.message.hearing.prosecutionCases.find { it.defendants.contains(defendant) }
                ?.prosecutionCaseIdentifier?.caseURN ?: return@forEach

            telemetryProperties = telemetryProperties + ("caseURN" to caseUrn)

            // Under 10-year-old validation
            val dateOfBirth = defendant.personDefendant?.personDetails?.dateOfBirth
            if (dateOfBirth == null || YEARS.between(dateOfBirth, LocalDate.now()) <= 10) {
                telemetryService.trackEvent("InvalidDateOfBirth", telemetryProperties)
                return@forEach
            }

            if (featureFlags.enabled("common-platform-record-creation-toggle")) {
                val remandedOffences = offenceService.getRemandOffences(defendant.offences, telemetryProperties)
                val mainOffence = offenceService.findMainOffence(remandedOffences) ?: return@forEach

                // Insert person and event
                val insertRemandDTO = InsertRemandDTO(
                    defendant = defendant,
                    mainOffence = mainOffence,
                    additionalOffences = remandedOffences.filter { it.offenceCode != mainOffence.offenceCode },
                    courtCode = notification.message.hearing.courtCentre.code,
                    sittingDay = notification.message.hearing.hearingDays.minOf { it.sittingDay },
                    caseUrn = caseUrn.replace("[^A-Za-z0-9]".toRegex(), ""),
                    hearingId = notification.message.hearing.id,
                )

                if (insertRemandDTO.sittingDay > ZonedDateTime.now()) {
                    telemetryService.trackEvent("FutureSittingDay", telemetryProperties)
                    return@forEach
                }

                if (personService.personWithDefendantIdExists(defendant.id)) {
                    telemetryService.trackEvent("PersonWithDefendantIdExists", telemetryProperties)
                    return@forEach
                }

                if (matchedPersonStatus.matchStatus == CorePersonRecordMatchStatus.POSSIBLE_MATCH) {
                    telemetryService.trackEvent("PossibleMatchCreationSkipped", telemetryProperties)
                    return@forEach
                }

                insertPersonAndEvent(insertRemandDTO)
            } else {
                telemetryService.trackEvent("SimulatedPersonCreated", telemetryProperties)
            }
        }
    }

    private fun insertPersonAndEvent(insertRemandDTO: InsertRemandDTO) {
        val (personResult, eventResult) = remandService.insertPersonOnRemand(insertRemandDTO)

        telemetryService.trackEvent(
            "PersonCreated", mapOf(
                "hearingId" to insertRemandDTO.hearingId,
                "defendantId" to insertRemandDTO.defendant.id,
                "CRN" to personResult.person.crn,
                "personId" to personResult.person.id.toString(),
                "personManagerId" to personResult.personManager.id.toString(),
                "equalityId" to personResult.equality.id.toString(),
                "addressId" to personResult.address?.id.toString(),
            )
        )

        telemetryService.trackEvent(
            "EventCreated", mapOf(
                "hearingId" to insertRemandDTO.hearingId,
                "eventId" to eventResult.event.id.toString(),
                "eventNumber" to eventResult.event.number,
                "CRN" to eventResult.event.person.crn,
                "personId" to eventResult.event.person.id.toString(),
                "orderManagerId" to eventResult.orderManager.id.toString(),
                "mainOffenceId" to eventResult.mainOffence.id.toString(),
                "courtAppearanceId" to eventResult.courtAppearance.id.toString(),
                "contactId" to eventResult.contact.id.toString()
            )
        )

        retry(3, delay = Duration.ofSeconds(1)) {
            corePersonClient.createPersonRecord(insertRemandDTO.defendant.id, personResult.toCorePersonRequest())
        }.apply {
            if (statusCode.is2xxSuccessful) {
                telemetryService.trackEvent(
                    "CPRRecordCreated", mapOf(
                        "hearingId" to insertRemandDTO.hearingId,
                        "defendantId" to insertRemandDTO.defendant.id,
                        "CRN" to personResult.person.crn
                    )
                )
            } else {
                Sentry.setExtra("defendantId", insertRemandDTO.defendant.id)
                Sentry.setExtra("CRN", personResult.person.crn)
                Sentry.captureException(RuntimeException("Failed to link core person record"))
                telemetryService.trackEvent(
                    "CPRCreateEndpointFailed", mapOf(
                        "defendantId" to insertRemandDTO.defendant.id,
                        "CRN" to personResult.person.crn
                    )
                )
            }
        }

        notifier.caseCreated(personResult.person)
        personResult.address?.let { notifier.addressCreated(it) }
    }

    private fun InsertPersonResult.toCorePersonRequest() = CreateCorePersonRequest(
        name = Name(
            forename = person.forename,
            middleName = person.secondName,
            surname = person.surname
        ),
        title = null,
        dateOfBirth = person.dateOfBirth,
        gender = CodeValue(person.gender.code),
        nationality = person.nationality?.code?.let { CodeValue(it) },
        secondaryNationality = person.secondNationality?.code?.let { CodeValue(it) },
        ethnicity = person.ethnicity?.code?.let { CodeValue(it) },
        identifiers = Identifiers(
            crn = person.crn,
            pnc = person.pncNumber,
            cro = person.croNumber
        ),
        contactDetails = ContactDetails(
            telephone = person.telephoneNumber,
            mobile = person.mobileNumber,
            email = person.email
        ),
        addresses = listOf(
            Address(
                fullAddress = listOfNotNull(
                    address?.buildingName,
                    address?.addressNumber,
                    address?.streetName,
                    address?.district,
                    address?.town,
                    address?.county
                ).joinToString(", "),
                postcode = address?.postcode,
                startDate = LocalDate.now(),
                endDate = null,
                noFixedAbode = address?.noFixedAbode ?: false
            )
        ),
        sentences = emptyList()
    )

    private val CorePersonRecordStatusResponse.telemetryProperties
        get() = mapOf(
            "matchStatus" to matchStatus.toString()
        )

    // Log sitting/hearing dates on incoming messages and set a flag if at least one date is in the future
    private val Notification<CommonPlatformHearing>.telemetryProperties
        get() = mapOf(
            "hearingId" to message.hearing.id,
            "hearingDates" to message.hearing.hearingDays.joinToString(", ") { it.sittingDay.toString() },
            "futureHearingDate" to message.hearing.hearingDays.maxBy { it.sittingDay }
                .sittingDay.isAfter(ZonedDateTime.now()).toString()
        )
}
