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
import java.time.ZonedDateTime

@Service
class RecommendationService(
    private val personRepository: PersonRepository,
    private val staffRepository: StaffRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactRepository: ContactRepository
) {

    fun managementOversight(
        crn: String,
        decision: ManagementDecision,
        details: RecommendationDetails,
        username: String,
        occurredAt: ZonedDateTime
    ) {
        val contact = personRepository.getPerson(crn).addContact(
            details,
            date = occurredAt,
            staff = staffRepository.getStaff(username),
            type = contactTypeRepository.getByCode(ContactType.MANAGEMENT_OVERSIGHT_RECALL),
            outcome = contactOutcomeRepository.getByCode(decision.code)
        )
        contactRepository.save(contact)
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
            staff = staffRepository.getStaff(username),
            type = contactTypeRepository.getByCode(ContactType.RECOMMENDATION_DELETED)
        )
        contactRepository.save(contact)
    }

    private fun Person.addContact(
        details: RecommendationDetails,
        staff: Staff,
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
            staffId = staff.id,
            outcome = outcome,
            isSensitive = details.sensitive
        )
    }
}
