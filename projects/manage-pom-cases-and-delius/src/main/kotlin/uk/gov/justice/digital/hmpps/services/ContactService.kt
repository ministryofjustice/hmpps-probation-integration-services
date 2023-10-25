package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getCurrentCom
import java.time.ZonedDateTime

@Service
class ContactService(
    private val personManagerRepository: PersonManagerRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository
) {
    fun createContact(personId: Long, typeCode: ContactType.Code, dateTime: ZonedDateTime) {
        val com = personManagerRepository.getCurrentCom(personId)
        val type = contactTypeRepository.getByCode(typeCode.value)
        contactRepository.save(
            Contact(
                personId,
                type,
                dateTime.toLocalDate(),
                dateTime,
                com.providerId,
                com.team.id,
                com.staff.id
            )
        )
    }
}
