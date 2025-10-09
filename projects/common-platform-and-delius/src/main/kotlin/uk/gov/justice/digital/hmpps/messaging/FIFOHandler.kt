package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.opensearch.client.opensearch.OpenSearchClient
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.dto.InsertRemandDTO
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.client.*
import uk.gov.justice.digital.hmpps.integrations.client.Address
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.retry.retry
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.service.RemandService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.ZonedDateTime

@Component
@Channel("common-platform-and-delius-fifo-queue")
class FIFOHandler(
    override val converter: NotificationConverter<CommonPlatformHearing>,
    private val telemetryService: TelemetryService,
    private val personService: PersonService,
    private val featureFlags: FeatureFlags,
    private val remandService: RemandService,
    private val openSearchClient: OpenSearchClient,
    private val offenceService: OffenceService,
    private val corePerson: CorePersonClient,
    private val notifier: Notifier
) : NotificationHandler<CommonPlatformHearing> {

    @Publish(messages = [Message(title = "COMMON_PLATFORM_HEARING", payload = Schema(CommonPlatformHearing::class))])
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.notificationReceived(notification)

        openSearchClient.index {
            it.index("court_messages").document(notification.message).id(notification.id.toString())
        }

        // Filter hearing message for defendants containing a judicial result label of 'Remanded in custody'
        val defendants = notification.message.hearing.prosecutionCases.flatMap { it.defendants }.filter { defendant ->
            defendant.offences.any { offence ->
                offence.judicialResults?.any { judicialResult ->
                    judicialResult.label == "Remanded in custody"
                } == true
            }
        }
        if (defendants.isEmpty()) {
            return
        }

        // Log sitting/hearing dates on incoming messages and set a flag if at least one date is in the future
        telemetryService.trackEvent(
            "HearingDatesDebug", mapOf(
                "hearingId" to notification.message.hearing.id,
                "hearingDates" to notification.message.hearing.hearingDays.joinToString(", ") { it.sittingDay.toString() },
                "futureHearingDate" to notification.message.hearing.hearingDays.maxBy { it.sittingDay }
                    .sittingDay.isAfter(ZonedDateTime.now()).toString()
            )
        )

        defendants.forEach { defendant ->

            val matchedPerson = corePerson.findByDefendantId(defendant.id)

            if (matchedPerson.identifiers.crns.isNotEmpty()) {
                telemetryService.trackEvent(
                    "ProbationSearchMatchDetected", mapOf(
                        "hearingId" to notification.message.hearing.id,
                        "defendantId" to defendant.id,
                        "crns" to matchedPerson.identifiers.crns.joinToString(", ", prefix = "[", postfix = "]")
                    )
                )
                return@forEach
            }

            // Under 10 years old validation
            matchedPerson.dateOfBirth?.also {
                val age = Period.between(it, LocalDate.now()).years
                require(age > 10) {
                    "Date of birth would indicate person is under ten years old: $it"
                }
            }

            if (featureFlags.enabled("common-platform-record-creation-toggle")) {
                val remandedOffences = defendant.offences.filter { offence ->
                    offence.judicialResults?.any { it.label == "Remanded in custody" } == true
                }

                val mainOffence = offenceService.findMainOffence(remandedOffences) ?: return@forEach

                val caseUrn = notification.message.hearing.prosecutionCases.find { it.defendants.contains(defendant) }
                    ?.prosecutionCaseIdentifier?.caseURN ?: return@forEach

                // Insert person and event
                val insertRemandDTO = InsertRemandDTO(
                    defendant = defendant,
                    courtCode = notification.message.hearing.courtCentre.code,
                    hearingOffence = mainOffence,
                    sittingDay = notification.message.hearing.hearingDays.first().sittingDay,
                    caseUrn = caseUrn,
                    hearingId = notification.message.hearing.id,
                    remandedOffences.filter { it.offenceCode != mainOffence.offenceCode }
                )

                if (personService.personWithDefendantIdExists(defendant.id)) return

                insertPersonAndEvent(insertRemandDTO)
            } else {
                telemetryService.trackEvent(
                    "SimulatedPersonCreated", mapOf(
                        "hearingId" to notification.message.hearing.id, "defendantId" to defendant.id
                    )
                )
            }
        }
    }

    private fun insertPersonAndEvent(insertRemandDTO: InsertRemandDTO) {
        val insertRemandResult = remandService.insertPersonOnRemand(insertRemandDTO)

        telemetryService.trackEvent(
            "PersonCreated", mapOf(
                "hearingId" to insertRemandDTO.hearingId,
                "defendantId" to insertRemandDTO.defendant.id,
                "CRN" to insertRemandResult.insertPersonResult.person.crn,
                "personId" to insertRemandResult.insertPersonResult.person.id.toString(),
                "personManagerId" to insertRemandResult.insertPersonResult.personManager.id.toString(),
                "equalityId" to insertRemandResult.insertPersonResult.equality.id.toString(),
                "addressId" to insertRemandResult.insertPersonResult.address?.id.toString(),
            )
        )

        telemetryService.trackEvent(
            "EventCreated", mapOf(
                "hearingId" to insertRemandDTO.hearingId,
                "eventId" to insertRemandResult.insertEventResult.event.id.toString(),
                "eventNumber" to insertRemandResult.insertEventResult.event.number,
                "CRN" to insertRemandResult.insertEventResult.event.person.crn,
                "personId" to insertRemandResult.insertEventResult.event.person.id.toString(),
                "orderManagerId" to insertRemandResult.insertEventResult.orderManager.id.toString(),
                "mainOffenceId" to insertRemandResult.insertEventResult.mainOffence.id.toString(),
                "courtAppearanceId" to insertRemandResult.insertEventResult.courtAppearance.id.toString(),
                "contactId" to insertRemandResult.insertEventResult.contact.id.toString()
            )
        )

        val cprRequest = CreateCorePersonRequest(
            name = Name(
                forename = insertRemandResult.insertPersonResult.person.forename,
                middleName = insertRemandResult.insertPersonResult.person.secondName,
                surname = insertRemandResult.insertPersonResult.person.surname
            ),
            title = null,
            dateOfBirth = insertRemandResult.insertPersonResult.person.dateOfBirth,
            gender = CodeValue(code = insertRemandResult.insertPersonResult.person.gender.code),
            nationality = null,
            identifiers = NewIdentifiers(
                crn = insertRemandResult.insertPersonResult.person.crn,
                pnc = insertRemandResult.insertPersonResult.person.pncNumber,
                cro = insertRemandResult.insertPersonResult.person.croNumber
            ),
            addresses = listOf(
                Address(
                    fullAddress = listOfNotNull(
                        insertRemandResult.insertPersonResult.address?.buildingName,
                        insertRemandResult.insertPersonResult.address?.addressNumber,
                        insertRemandResult.insertPersonResult.address?.streetName,
                        insertRemandResult.insertPersonResult.address?.district,
                        insertRemandResult.insertPersonResult.address?.town,
                        insertRemandResult.insertPersonResult.address?.county
                    ).joinToString(", "),
                    postcode = insertRemandResult.insertPersonResult.address?.postcode,
                    startDate = LocalDate.now(),
                    endDate = null,
                    noFixedAbode = insertRemandResult.insertPersonResult.address?.noFixedAbode ?: false
                )
            ),
            sentences = emptyList()
        )

        val cprResponse = retry(3, delay = Duration.ofSeconds(1)) {
            corePerson.createPersonRecord(insertRemandDTO.defendant.id, cprRequest)
        }

        if (cprResponse.statusCode.is2xxSuccessful) {
            telemetryService.trackEvent(
                "CPRRecordCreated", mapOf(
                    "hearingId" to insertRemandDTO.hearingId,
                    "defendantId" to insertRemandDTO.defendant.id,
                    "CRN" to insertRemandResult.insertPersonResult.person.crn
                )
            )
        } else {
            telemetryService.trackEvent(
                "CPRCreateEndpointFailed", mapOf(
                    "defendantId" to insertRemandDTO.defendant.id,
                    "CRN" to insertRemandResult.insertPersonResult.person.crn
                )
            )
        }

        notifier.caseCreated(insertRemandResult.insertPersonResult.person)
        insertRemandResult.insertPersonResult.address?.let { notifier.addressCreated(it) }
    }
}
