package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.messaging.OpdAssessment

@Service
class ContactService(
    private val contactRepository: ContactRepository,
) {
    fun createContact(
        com: PersonManager,
        type: ContactType?,
        opdAssessment: OpdAssessment,
    ) {
        type?.also {
            contactRepository.save(
                Contact(
                    com.person.id,
                    it,
                    opdAssessment.date.toLocalDate(),
                    opdAssessment.date,
                    opdAssessment.notes,
                    com.teamId,
                    com.staffId,
                ),
            )
        }
    }
}
