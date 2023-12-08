package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.LocalDate

@Service
class ContactService(
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository
) {
    fun createContact(detail: ContactDetail, person: Person, event: Event? = null): Contact =
        contactRepository.save(
            Contact(detail.contactType ?: contactTypeRepository.getByCode(detail.typeCode.value), person, event?.id)
                .withNotes(detail.notes)
                .withDateTeamAndStaff(detail.date, person.manager.teamId, person.manager.staffId)
        )
}

class ContactDetail(
    val typeCode: ContactType.Code,
    val date: LocalDate = LocalDate.now(),
    val notes: String,
    val contactType: ContactType? = null
)
