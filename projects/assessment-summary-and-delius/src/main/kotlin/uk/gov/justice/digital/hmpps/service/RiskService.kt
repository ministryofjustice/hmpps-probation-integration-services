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
import uk.gov.justice.digital.hmpps.service.TelemetryAggregator.Companion.DEREGISTERED
import uk.gov.justice.digital.hmpps.service.TelemetryAggregator.Companion.REGISTERED
import uk.gov.justice.digital.hmpps.service.TelemetryAggregator.Companion.REVIEW_COMPLETED
import java.time.LocalDate

@Service
class RiskService(
    private val registrationRepository: RegistrationRepository,
    private val registrationHistoryRepository: RegistrationHistoryRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactService: ContactService,
    private val ordsClient: OrdsClient,
    private val domainEventService: DomainEventService,
) {
    fun activeVisorAndMappa(person: Person): Boolean {
        val registerCounts: Map<String, Int> =
            registrationRepository.hasVisorAndMappa(person.id).associate { it.type to it.number }
        return registerCounts.getOrDefault(RegisterType.Code.VISOR.value, 0) > 0 &&
            registerCounts.getOrDefault(RegisterType.Code.MAPPA.value, 0) > 0
    }

    fun recordRisk(person: Person, summary: AssessmentSummary, telemetryRecording: (String, String) -> Unit) {
        recordRiskOfSeriousHarm(person, summary, telemetryRecording)
        recordOtherRisks(person, summary, telemetryRecording)
    }

    private fun recordRiskOfSeriousHarm(
        person: Person,
        summary: AssessmentSummary,
        addToTelemetry: (String, String) -> Unit
    ) {
        val roshType = summary.riskFlags.mapNotNull(RiskOfSeriousHarmType::of).maxByOrNull { it.ordinal } ?: return
        val roshRegistrations = registrationRepository.findByPersonIdAndTypeFlagCode(person.id, OASYS_RISK_FLAG.value)

        val (matchingRegistrations, registrationsToRemove) = roshRegistrations.partition { it.type.code == roshType.code }

        // Remove any existing RoSH registrations of a different type
        registrationsToRemove.forEach {
            person.removeRegistration(it, it.notes(), addToTelemetry)
        }

        // If no matching RoSH registration of the correct type, create one
        if (matchingRegistrations.isEmpty()) {
            val type = registerTypeRepository.getByCode(roshType.code)
            person.addRegistration(type, addToTelemetry = addToTelemetry)
        }

        // Set the RoSH flag on the person
        person.highestRiskColour = registrationRepository
            .findByPersonIdAndTypeFlagCode(person.id, OASYS_RISK_FLAG.value)
            .firstOrNull { !it.deregistered }?.type?.colour
    }

    private fun recordOtherRisks(
        person: Person,
        summary: AssessmentSummary,
        addToTelemetry: (String, String) -> Unit
    ) {
        RiskType.entries.forEach { riskType ->
            val riskLevel = riskType.riskLevel(summary) ?: return@forEach
            val registration = registrationRepository.findByPersonIdAndTypeCode(person.id, riskType.code).firstOrNull()

            // Deregister if OASys identified the level as low risk
            if (registration != null && riskLevel == RiskLevel.L) {
                person.removeRegistration(registration, addToTelemetry = addToTelemetry)
                return@forEach
            }

            // Add/update registration
            val type = registerTypeRepository.getByCode(riskType.code)
            val level = referenceDataRepository.registerLevel(riskLevel.code)
            val (assessmentNote, riskNotes) = summary.riskNotes(registration, type, level)
            if (registration == null) {
                person.addRegistration(type, level, riskNotes, assessmentNote, addToTelemetry)
            } else {
                person.updateRegistration(registration, level, riskNotes, assessmentNote, addToTelemetry)
            }
        }
    }

    private fun Person.addRegistration(
        type: RegisterType,
        level: ReferenceData? = null,
        riskNotes: String? = null,
        assessmentNote: String? = null,
        addToTelemetry: (String, String) -> Unit,
        date: LocalDate = LocalDate.now()
    ) {
        val nextReviewDate = type.reviewPeriod?.let { date.plusMonths(it) }
        val contact = contactService.createContact(
            person = this,
            detail = ContactDetail(
                ContactType.Code.REGISTRATION,
                notes = assessmentNote?.let { "$assessmentNote\n\nSee Register for more details" }
                    ?: reviewNotes(type, nextReviewDate),
                description = "Registration of type ${type.description} added"
            )
        )
        val registration = registrationRepository.save(
            Registration(
                personId = this.id,
                date = date,
                contact = contact,
                type = type,
                level = level,
                initialLevel = level,
                nextReviewDate = nextReviewDate,
                notes = riskNotes,
            )
        )
        registration.addHistory(date)
        registration.addReview(this)
        registration.removeDuplicateGroupTypes(this, addToTelemetry)
        domainEventService.publishRegistrationAdded(this.crn, registration)
        addToTelemetry(REGISTERED, type.code)
    }

    private fun Person.updateRegistration(
        registration: Registration,
        level: ReferenceData,
        riskNotes: String,
        assessmentNote: String,
        addToTelemetry: (String, String) -> Unit,
        date: LocalDate = LocalDate.now(),
    ) {
        registration.notes = listOfNotNull(registration.notes, riskNotes).joinToString("\n")
        if (registration.level?.code != level.code) {
            registration.level = level
            registration.addHistory(date)
        }

        // Complete any open reviews and add a new one
        registration.nextReviewDate = registration.type.reviewPeriod?.let { date.plusMonths(it) }
        registration.reviews.filter { !it.completed }.forEach { review ->
            review.completed = true
            review.date = date
            review.reviewDue = registration.nextReviewDate
            review.notes = listOfNotNull(review.notes, riskNotes).joinToString("\n")
            review.contact.withNotes("$assessmentNote\n\nSee review for more details")
            addToTelemetry(REVIEW_COMPLETED, registration.type.code)
        }
        registration.addReview(this)

        domainEventService.publishRegistrationUpdate(this.crn, registration)
    }

    private fun Person.removeRegistration(
        registration: Registration,
        notes: String = registration.type.notes(),
        addToTelemetry: (String, String) -> Unit,
    ) {
        val contact = contactService.createContact(ContactDetail(DEREGISTRATION, notes = notes), this)
        registration.deregistration = DeRegistration(
            date = LocalDate.now(),
            registration = registration,
            personId = this.id,
            contact = contact,
            teamId = contact.teamId,
            staffId = contact.staffId
        )
        registration.deregistered = true
        registration.nextReviewDate = null
        registration.reviews.removeIf { !it.completed && it.notes.isNullOrBlank() && it.lastUpdatedDatetime.isEqual(it.createdDatetime) }
        registration.reviews.lastOrNull()?.let {
            it.reviewDue = null
            if (!it.completed) {
                it.completed = true
                addToTelemetry(REVIEW_COMPLETED, registration.type.code)
            }
        }
        addToTelemetry(DEREGISTERED, registration.type.code)
        domainEventService.publishDeregistration(crn, registration.deregistration!!)
    }

    private fun Registration.addHistory(date: LocalDate) {
        registrationHistoryRepository.findByRegistrationIdAndEndDateIsNull(id)?.apply { endDate = date }
        registrationHistoryRepository.flush()
        registrationHistoryRepository.save(RegistrationHistory(this, date))
    }

    private fun Registration.removeDuplicateGroupTypes(person: Person, addToTelemetry: (String, String) -> Unit) {
        registerTypeRepository.findOtherTypesInGroup(type.code)
            .let { registrationRepository.findByPersonIdAndTypeCodeIn(person.id, it) }
            .forEach { registration ->
                person.removeRegistration(
                    registration,
                    "De-registered when a ${type.description} registration was added on ${date.toDeliusDate()}.",
                    addToTelemetry
                )
            }
    }

    private fun Registration.addReview(person: Person) {
        val contact = contactService.createContact(
            person = person,
            detail = ContactDetail(
                typeCode = ContactType.Code.REGISTRATION_REVIEW,
                date = nextReviewDate ?: LocalDate.now(),
                notes = notes(),
                description = "Registration Review of Register of type ${type.description}"
            ),
        )
        reviews += RegistrationReview(
            registration = this,
            contact = contact,
            date = nextReviewDate,
            reviewDue = null,
            teamId = teamId,
            staffId = staffId,
            notes = contact.notes,
            category = category,
            level = level
        )
    }

    private fun AssessmentSummary.riskNotes(
        registration: Registration?,
        type: RegisterType,
        level: ReferenceData
    ): Pair<String, String> {
        fun RiskLevel?.increasedOrDecreasedTo(riskLevel: RiskLevel?) = when {
            this == null || riskLevel == null -> "to be"
            this.ordinal < riskLevel.ordinal -> "to have increased to"
            this.ordinal > riskLevel.ordinal -> "to have decreased to"
            else -> "to have remained"
        }

        val roshSummary = ordsClient.getRoshSummary(assessmentPk)?.assessments?.singleOrNull()
        val existingLevel = registration?.level?.code?.let { RiskLevel.byCode(it) }
        val assessmentNote = "The OASys assessment of ${furtherInformation.pOAssessmentDesc} on " +
            "${dateCompleted.toDeliusDate()} identified the ${type.description} " +
            "${existingLevel.increasedOrDecreasedTo(RiskLevel.byCode(level.code))} ${level.description}."
        val riskNotes = """
            |$assessmentNote
            |${roshSummary?.whoAtRisk?.let { "\n|*R10.1 Who is at risk*\n|$it" }}
            |${roshSummary?.natureOfRisk?.let { "\n|*R10.2 What is the nature of the risk*\n|$it" }}
            """.trimMargin()
        return Pair(assessmentNote, riskNotes)
    }

    fun RegisterType.notes() = "Type: ${flag.description} - $description"

    fun reviewNotes(type: RegisterType, nextReviewDate: LocalDate?) = listOfNotNull(
        type.notes(),
        nextReviewDate?.let { "Next Review Date: ${it.toDeliusDate()}" }
    ).joinToString(System.lineSeparator())

    fun Registration.notes(): String = reviewNotes(type, nextReviewDate)
}
