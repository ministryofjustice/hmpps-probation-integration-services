package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.enum.RiskLevel
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType
import uk.gov.justice.digital.hmpps.enum.RiskType
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
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
import java.time.LocalDate

@Service
class RiskService(
    private val registrationRepository: RegistrationRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactService: ContactService,
    private val ordsClient: OrdsClient,
    private val featureFlags: FeatureFlags
) {
    fun recordRisk(person: Person, summary: AssessmentSummary) =
        recordRiskOfSeriousHarm(person, summary) + recordOtherRisks(person, summary)

    private fun recordRiskOfSeriousHarm(person: Person, summary: AssessmentSummary): List<HmppsDomainEvent> {
        val roshType = summary.riskFlags.mapNotNull(RiskOfSeriousHarmType::of).maxByOrNull { it.ordinal }
            ?: return emptyList()

        val registrations = registrationRepository
            .findByPersonIdAndTypeFlagCode(person.id, OASYS_RISK_FLAG.value)
            .toMutableList()

        val (matchingRegistrations, registrationsToRemove) = registrations.partition { it.type.code == roshType.code }

        val deRegEvents = registrationsToRemove.map {
            val contact = contactService.createContact(ContactDetail(DEREGISTRATION, notes = it.notes()), person)
            it.deregister(contact)
            it.deRegEvent(person.crn)
        }
        val regEvent = if (matchingRegistrations.isEmpty()) {
            val type = registerTypeRepository.getByCode(roshType.code)
            val registration = registrations.addRegistration(person, type)
            registration
                .withReview(registration.reviewContact(person))
                .regEvent(person.crn)
        } else null

        person.highestRiskColour = registrations.firstOrNull { !it.deregistered }?.type?.colour

        return listOfNotNull(regEvent) + deRegEvents
    }

    private fun recordOtherRisks(person: Person, summary: AssessmentSummary): List<HmppsDomainEvent> {
        if (!featureFlags.enabled("assessment-summary-additional-risks")) return emptyList()
        return RiskType.entries.flatMap { riskType ->
            val riskLevel = riskType.riskLevel(summary) ?: return@flatMap emptyList()
            val registrations =
                registrationRepository.findByPersonIdAndTypeCode(person.id, riskType.code).toMutableList()

            // Deregister existing registrations if OASys identified the level as low risk
            if (riskLevel == RiskLevel.L) return@flatMap registrations.map { person.removeRegistration(it) }

            // Remove any existing registrations with a different level
            val (matchingRegistrations, registrationsToRemove) = registrations
                .partition { it.level != null && it.level.code == riskLevel.code }
            val events = registrationsToRemove.map { person.removeRegistration(it) }.toMutableList()

            // Add registration with the identified level if it doesn't already exist
            if (matchingRegistrations.isEmpty()) {
                val type = registerTypeRepository.getByCode(riskType.code)
                val level = referenceDataRepository.registerLevel(riskLevel.code)
                val roshSummary = ordsClient.getRoshSummary(summary.assessmentPk)?.assessments?.singleOrNull()

                val existingLevel = RiskLevel.maxByCode(registrationsToRemove.mapNotNull { it.level?.code })
                val notes = """
                |The OASys assessment of ${summary.furtherInformation.pOAssessmentDesc} on ${summary.dateCompleted.toDeliusDate()} identified the ${type.description} ${
                    when {
                        existingLevel == null || existingLevel.ordinal == riskLevel.ordinal -> "to be"
                        existingLevel.ordinal < riskLevel.ordinal -> "to have increased to"
                        else -> "to have decreased to"
                    }
                } ${level.description}.
                |${roshSummary?.whoAtRisk?.let { "\n|*R10.1 Who is at risk*\n|$it" }}
                |${roshSummary?.natureOfRisk?.let { "\n|*R10.2 What is the nature of the risk*\n|$it" }}
                """.trimMargin()
                events += registrations.addRegistration(person, type, level, notes).regEvent(person.crn)

                // Registrations in the type's duplicate group should also be removed
                registerTypeRepository.findOtherTypesInGroup(riskType.code)
                    .let { registrationRepository.findByPersonIdAndTypeCodeIn(person.id, it) }
                    .forEach {
                        val duplicateGroupNotes =
                            "De-registered when a ${type.description} registration was added on ${summary.dateCompleted.toDeliusDate()}."
                        events += person.removeRegistration(it, notes = duplicateGroupNotes)
                    }
            }

            // Always add a review, regardless of whether the register level has changed
            registrationRepository.findByPersonIdAndTypeCode(person.id, riskType.code).singleOrNull()
                ?.let { it.withReview(it.reviewContact(person)) }

            return@flatMap events
        }
    }

    private fun MutableList<Registration>.addRegistration(
        person: Person,
        type: RegisterType,
        level: ReferenceData? = null,
        notes: String? = null,
    ): Registration {
        val nextReviewDate = type.reviewPeriod?.let { LocalDate.now().plusMonths(it) }

        val contact = contactService.createContact(
            ContactDetail(
                ContactType.Code.REGISTRATION,
                notes = reviewNotes(type, nextReviewDate),
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

    private fun Person.removeRegistration(it: Registration, notes: String? = null): HmppsDomainEvent {
        val contact = contactService.createContact(ContactDetail(DEREGISTRATION, notes = notes ?: it.notes()), this)
        it.deregister(contact)
        return it.deRegEvent(crn)
    }

    private fun Registration.reviewContact(person: Person) = contactService.createContact(
        ContactDetail(
            ContactType.Code.REGISTRATION_REVIEW,
            notes = notes(),
            contactType = type.reviewContactType
        ),
        person
    )
}

fun reviewNotes(type: RegisterType, nextReviewDate: LocalDate?) = listOfNotNull(
    "Type: ${type.flag.description} - ${type.description}",
    nextReviewDate?.let { "Next Review Date: ${it.toDeliusDate()}" }
).joinToString(System.lineSeparator())

fun Registration.notes(): String = reviewNotes(type, nextReviewDate)
