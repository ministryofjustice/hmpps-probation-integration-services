package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.enum.RiskLevel
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType
import uk.gov.justice.digital.hmpps.enum.RiskType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.DEREGISTRATION
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData.Code.OASYS_RISK_FLAG
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.registerLevel
import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary
import uk.gov.justice.digital.hmpps.integrations.oasys.OrdsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.service.TelemetryAggregator.Companion.DEREGISTERED
import uk.gov.justice.digital.hmpps.service.TelemetryAggregator.Companion.REGISTERED
import uk.gov.justice.digital.hmpps.service.TelemetryAggregator.Companion.REVIEW_COMPLETED
import java.time.LocalDate

@Service
class RiskService(
    private val registrationRepository: RegistrationRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactService: ContactService,
    private val ordsClient: OrdsClient
) {
    fun recordRisk(person: Person, summary: AssessmentSummary, telemetryRecording: (String, String) -> Unit) =
        recordRiskOfSeriousHarm(person, summary, telemetryRecording) + recordOtherRisks(
            person,
            summary,
            telemetryRecording
        )

    private fun recordRiskOfSeriousHarm(
        person: Person,
        summary: AssessmentSummary,
        addToTelemetry: (String, String) -> Unit
    ): List<HmppsDomainEvent> {
        val roshType = summary.riskFlags.mapNotNull(RiskOfSeriousHarmType::of).maxByOrNull { it.ordinal }
            ?: return emptyList()

        val registrations = registrationRepository
            .findByPersonIdAndTypeFlagCode(person.id, OASYS_RISK_FLAG.value)
            .toMutableList()

        val (matchingRegistrations, registrationsToRemove) = registrations.partition { it.type.code == roshType.code }

        val deRegEvents = registrationsToRemove.map {
            val contact = contactService.createContact(ContactDetail(DEREGISTRATION, notes = it.notes()), person)
            val reviewCompleted = it.deregister(contact)
            if (reviewCompleted) {
                addToTelemetry(REVIEW_COMPLETED, it.type.code)
            }
            addToTelemetry(DEREGISTERED, it.type.code)
            it.deRegEvent(person.crn)
        }

        val regEvent = if (matchingRegistrations.isEmpty()) {
            val type = registerTypeRepository.getByCode(roshType.code)
            val registration = registrations.addRegistration(person, type)
            addToTelemetry(REGISTERED, type.code)
            registration
                .withReview(registration.reviewContact(person))
                .regEvent(person.crn)
        } else null

        person.highestRiskColour = registrations.firstOrNull { !it.deregistered }?.type?.colour

        return listOfNotNull(regEvent) + deRegEvents
    }

    private fun recordOtherRisks(
        person: Person,
        summary: AssessmentSummary,
        addToTelemetry: (String, String) -> Unit
    ): List<HmppsDomainEvent> =
        RiskType.entries.flatMap { riskType ->
            val riskLevel = riskType.riskLevel(summary) ?: return@flatMap emptyList()
            val registrations =
                registrationRepository.findByPersonIdAndTypeCode(person.id, riskType.code).toMutableList()

            // Deregister existing registrations if OASys identified the level as low risk
            if (riskLevel == RiskLevel.L) return@flatMap registrations.map {
                person.removeRegistration(it, addToTelemetry = addToTelemetry)
            }

            // Add the level to any existing registrations with no level
            val level = referenceDataRepository.registerLevel(riskLevel.code)
            registrations.filter { it.level == null }.forEach { it.level = level }

            // Remove any existing registrations with a different level
            val (matchingRegistrations, registrationsToRemove) = registrations.partition { it.level!!.code == riskLevel.code }
            val events = registrationsToRemove.map {
                person.removeRegistration(it, addToTelemetry = addToTelemetry)
            }.toMutableList()

            // Get RoSH answers from OASys
            val type = registerTypeRepository.getByCode(riskType.code)
            val existingLevel = RiskLevel.maxByCode(registrationsToRemove.mapNotNull { it.level?.code })
            val assessmentNote = "The OASys assessment of ${summary.furtherInformation.pOAssessmentDesc} on " +
                "${summary.dateCompleted.toDeliusDate()} identified the ${type.description} " +
                "${existingLevel.increasedOrDecreasedTo(riskLevel)} ${level.description}."
            val roshSummary = ordsClient.getRoshSummary(summary.assessmentPk)?.assessments?.singleOrNull()
            val roshNotes = """
                    |$assessmentNote
                    |${roshSummary?.whoAtRisk?.let { "\n|*R10.1 Who is at risk*\n|$it" }}
                    |${roshSummary?.natureOfRisk?.let { "\n|*R10.2 What is the nature of the risk*\n|$it" }}
                    """.trimMargin()

            // Add registration with the identified level if it doesn't already exist
            if (matchingRegistrations.isEmpty()) {
                val contactNotes = "$assessmentNote\n\nSee Register for more details"
                events += registrations.addRegistration(person, type, level, roshNotes, contactNotes)
                    .regEvent(person.crn)
                addToTelemetry(REGISTERED, type.code)

                // Registrations in the type's duplicate group should also be removed
                registerTypeRepository.findOtherTypesInGroup(riskType.code)
                    .let { registrationRepository.findByPersonIdAndTypeCodeIn(person.id, it) }
                    .forEach {
                        val duplicateGroupNotes =
                            "De-registered when a ${type.description} registration was added on ${summary.dateCompleted.toDeliusDate()}."
                        events += person.removeRegistration(it, duplicateGroupNotes, addToTelemetry)
                    }
            }

            // Complete any open reviews and add a new one, regardless of whether the level has changed
            registrationRepository.findByPersonIdAndTypeCode(person.id, riskType.code).singleOrNull()?.let {
                it.nextReviewDate = it.type.reviewPeriod?.let { LocalDate.now().plusMonths(it) }
                it.reviews.filter { !it.completed }.forEach { review ->
                    addToTelemetry(REVIEW_COMPLETED, type.code)
                    review.completed = true
                    review.date = LocalDate.now()
                    review.reviewDue = it.nextReviewDate
                    review.notes = review.notes?.let { "$it\n$roshNotes" } ?: roshNotes
                    review.contact.withNotes("$assessmentNote\n\nSee review for more details")
                }
                it.withReview(it.reviewContact(person))
            }

            return@flatMap events
        }

    private fun MutableList<Registration>.addRegistration(
        person: Person,
        type: RegisterType,
        level: ReferenceData? = null,
        notes: String? = null,
        contactNotes: String? = null,
    ): Registration {
        val nextReviewDate = type.reviewPeriod?.let { LocalDate.now().plusMonths(it) }

        val contact = contactService.createContact(
            ContactDetail(
                ContactType.Code.REGISTRATION,
                notes = contactNotes ?: reviewNotes(type, nextReviewDate),
                contactType = type.registrationContactType
            ),
            person
        )
        val registration = registrationRepository.save(
            Registration(
                personId = person.id,
                date = LocalDate.now(),
                contact = contact,
                teamId = contact.teamId,
                staffId = contact.staffId,
                type = type,
                level = level,
                nextReviewDate = nextReviewDate,
                notes = notes,
            )
        )
        this += registration
        return registration
    }

    private fun Person.removeRegistration(
        registration: Registration,
        notes: String = registration.type.notes(),
        addToTelemetry: (String, String) -> Unit
    ): HmppsDomainEvent {
        val contact = contactService.createContact(ContactDetail(DEREGISTRATION, notes = notes), this)
        val reviewCompleted = registration.deregister(contact)
        addToTelemetry(DEREGISTERED, registration.type.code)
        if (reviewCompleted) {
            addToTelemetry(REVIEW_COMPLETED, registration.type.code)
        }
        return registration.deRegEvent(crn)
    }

    private fun Registration.reviewContact(person: Person, notes: String = notes()) = contactService.createContact(
        ContactDetail(
            ContactType.Code.REGISTRATION_REVIEW,
            notes = notes,
            contactType = type.reviewContactType
        ),
        person
    )
}

fun RegisterType.notes() = "Type: ${flag.description} - $description"

fun reviewNotes(type: RegisterType, nextReviewDate: LocalDate?) = listOfNotNull(
    type.notes(),
    nextReviewDate?.let { "Next Review Date: ${it.toDeliusDate()}" }
).joinToString(System.lineSeparator())

fun Registration.notes(): String = reviewNotes(type, nextReviewDate)

private fun RiskLevel?.increasedOrDecreasedTo(riskLevel: RiskLevel) = when {
    this == null -> "to be"
    this.ordinal < riskLevel.ordinal -> "to have increased to"
    this.ordinal > riskLevel.ordinal -> "to have decreased to"
    else -> "to have remained"
}
