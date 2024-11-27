package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
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
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.time.LocalDate

@Service
class RiskService(
    private val registrationRepository: RegistrationRepository,
    private val registerTypeRepository: RegisterTypeRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactService: ContactService,
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
            val desiredLevel = riskType.riskLevel(summary) ?: return@flatMap emptyList()
            val registrations =
                registrationRepository.findByPersonIdAndTypeCode(person.id, riskType.code).toMutableList()

            val (matchingRegistrations, registrationsToRemove) = registrations.partition { existing ->
                existing.level != null && existing.level.code == desiredLevel.code && desiredLevel != RiskLevel.L
            }

            val deRegEvents = registrationsToRemove.map {
                val contact = contactService.createContact(ContactDetail(DEREGISTRATION, notes = it.notes()), person)
                it.deregister(contact)
                it.deRegEvent(person.crn)
            }
            val regEvent = if (matchingRegistrations.isEmpty() && desiredLevel != RiskLevel.L) {
                val type = registerTypeRepository.getByCode(riskType.code)
                val level = referenceDataRepository.registerLevel(desiredLevel.code)
                val notes =
                    "The OASys assessment of ${summary.furtherInformation.pOAssessmentDesc} on ${
                        DeliusDateFormatter.format(
                            summary.dateCompleted
                        )
                    } identified the ${type.description} to be ${level.description}"
                val registration = registrations.addRegistration(person, type, level, notes)
                registration.regEvent(person.crn)
            } else null

            if (desiredLevel != RiskLevel.L) {
                // Always add a review if medium or higher, regardless of whether the register level has changed
                registrationRepository.findByPersonIdAndTypeCode(person.id, riskType.code).singleOrNull()
                    ?.let { it.withReview(it.reviewContact(person)) }
            }

            return@flatMap listOfNotNull(regEvent) + deRegEvents
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
    nextReviewDate?.let { "Next Review Date: ${DeliusDateFormatter.format(it)}" }
).joinToString(System.lineSeparator())

fun Registration.notes(): String = reviewNotes(type, nextReviewDate)
