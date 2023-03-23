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
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider.getStaff
import uk.gov.justice.digital.hmpps.messaging.ManagementDecision
import java.time.ZonedDateTime

@Service
class ManagementOversightRecall(
    private val personRepository: PersonRepository,
    private val staffRepository: StaffRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactRepository: ContactRepository
) {

    fun decision(
        crn: String,
        recommendationUrl: String,
        occurredAt: ZonedDateTime,
        decision: ManagementDecision,
        staffCode: String
    ) {
        val person = personRepository.getPerson(crn)
        val staff = staffRepository.getStaff(staffCode)
        contactRepository.save(person.managementOversightRecall(recommendationUrl, occurredAt, decision, staff))
    }

    private fun Person.managementOversightRecall(
        recommendationUrl: String,
        occurredAt: ZonedDateTime,
        decision: ManagementDecision,
        staff: Staff
    ): Contact {
        if (manager == null) throw IllegalStateException("No Active Person Manager")
        return Contact(
            0,
            id,
            occurredAt,
            occurredAt,
            type = contactTypeRepository.getByCode(ContactType.MANAGEMENT_OVERSIGHT_RECALL),
            notes = "View details of the Manage a Recall Oversight Decision: $recommendationUrl",
            providerId = manager.providerId,
            teamId = manager.teamId,
            staffId = staff.id,
            outcome = contactOutcomeRepository.getByCode(decision.code)
        )
    }
}
