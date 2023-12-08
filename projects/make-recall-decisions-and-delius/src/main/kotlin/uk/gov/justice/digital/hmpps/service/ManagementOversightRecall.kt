package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.getStaff
import uk.gov.justice.digital.hmpps.integrations.makerecalldecisions.MakeRecallDecisionsClient.DecisionDetails
import uk.gov.justice.digital.hmpps.messaging.ManagementDecision
import java.time.ZonedDateTime

@Service
class ManagementOversightRecall(
    private val personRepository: PersonRepository,
    private val staffRepository: StaffRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactRepository: ContactRepository,
) {
    fun decision(
        crn: String,
        decision: ManagementDecision,
        details: DecisionDetails,
        username: String,
        occurredAt: ZonedDateTime,
    ) {
        val person = personRepository.getPerson(crn)
        val staff = staffRepository.getStaff(username)
        val contact = person.managementOversightRecall(decision, details.notes, details.sensitive, staff, occurredAt)
        contactRepository.save(contact)
    }

    private fun Person.managementOversightRecall(
        decision: ManagementDecision,
        notes: String,
        sensitive: Boolean,
        staff: Staff,
        occurredAt: ZonedDateTime,
    ): Contact {
        checkNotNull(manager) { "No Active Person Manager" }
        return Contact(
            0,
            id,
            occurredAt,
            occurredAt,
            type = contactTypeRepository.getByCode(ContactType.MANAGEMENT_OVERSIGHT_RECALL),
            notes = notes,
            providerId = manager.providerId,
            teamId = manager.teamId,
            staffId = staff.id,
            outcome = contactOutcomeRepository.getByCode(decision.code),
            isSensitive = sensitive,
        )
    }
}
