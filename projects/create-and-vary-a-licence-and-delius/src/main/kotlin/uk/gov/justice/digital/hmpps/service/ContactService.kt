package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Disposal
import java.time.ZonedDateTime

@Service
class ContactService(
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository
) {
    fun createContact(disposal: Disposal, com: PersonManager, startDateTime: ZonedDateTime): Contact {
        return contactRepository.save(
            Contact(
                disposal.event.person.id,
                disposal.event.id,
                startDateTime.toLocalDate(),
                startDateTime,
                contactTypeRepository.getByCode(ContactType.LPOP),
                com.team.id,
                com.staff.id,
                """
            |Delius has been updated with licence conditions entered in the Create and Vary a licence service.
                """.trimMargin()
            )
        )
    }
}
