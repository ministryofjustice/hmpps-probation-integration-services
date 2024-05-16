package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.getStaff
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.MakeRecallDecisionsClient.RecommendationDetails
import uk.gov.justice.digital.hmpps.messaging.ManagementDecision
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class RecommendationService(
    private val personRepository: PersonRepository,
    private val staffRepository: StaffRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactRepository: ContactRepository,
    private val telemetryService: TelemetryService,
) {

    fun managementOversight(
        crn: String,
        decision: ManagementDecision,
        details: RecommendationDetails,
        username: String,
        occurredAt: ZonedDateTime
    ) {
        val contact = addContact(crn, decision, details, username, occurredAt, ContactType.MANAGEMENT_OVERSIGHT_RECALL)
        contactRepository.save(contact)
        telemetryService.trackEvent(
            "ManagementOversightCreated",
            mapOf("CRN" to crn, "username" to username, "decision" to decision.name)
        )
    }

    fun consideration(
        crn: String,
        details: RecommendationDetails,
        username: String,
        occurredAt: ZonedDateTime
    ) {
        val contact = addContact(crn, null, details, username, occurredAt, ContactType.CONSIDERATION)
        contactRepository.save(contact)
        telemetryService.trackEvent(
            "ConsiderationCreated",
            mapOf("CRN" to crn, "username" to username)
        )
    }

    private fun addContact(
        crn: String,
        decision: ManagementDecision? = null,
        details: RecommendationDetails,
        username: String,
        occurredAt: ZonedDateTime,
        contactType: String
    ): Contact {
        return personRepository.getPerson(crn).addContact(
            details,
            date = occurredAt,
            staff = staffRepository.getStaff(username),
            type = contactTypeRepository.getByCode(contactType),
            outcome = decision?.let { contactOutcomeRepository.getByCode(it.code) }
        )
    }

    fun deletion(
        crn: String,
        details: RecommendationDetails,
        username: String,
        occurredAt: ZonedDateTime
    ) {
        val contact = personRepository.getPerson(crn).addContact(
            details,
            date = occurredAt,
            staff = staffRepository.findStaffByUsername(username),
            type = contactTypeRepository.getByCode(ContactType.RECOMMENDATION_DELETED)
        )
        contactRepository.save(contact)
        telemetryService.trackEvent("RecommendationDeletionCreated", mapOf("CRN" to crn, "username" to username))
    }

    private fun Person.addContact(
        details: RecommendationDetails,
        staff: Staff?,
        date: ZonedDateTime,
        type: ContactType,
        outcome: ContactOutcome? = null,
    ): Contact {
        checkNotNull(manager) { "No Active Person Manager" }
        return Contact(
            0,
            id,
            date,
            date,
            type = type,
            notes = details.notes,
            providerId = manager.providerId,
            teamId = manager.teamId,
            staffId = staff?.id ?: manager.staffId,
            outcome = outcome,
            isSensitive = details.sensitive
        )
    }
}
