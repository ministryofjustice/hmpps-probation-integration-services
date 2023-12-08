package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Manager
import java.time.ZonedDateTime

@Service
class ContactService(
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
) {
    fun createContact(
        personId: Long,
        typeCode: ContactType.Code,
        dateTime: ZonedDateTime,
        manager: Manager,
        notes: String?,
    ) {
        val type = contactTypeRepository.getByCode(typeCode.value)
        contactRepository.save(
            Contact(
                personId,
                type,
                dateTime.toLocalDate(),
                dateTime,
                notes,
                manager.probationArea.id,
                manager.team.id,
                manager.staff.id,
            ),
        )
    }
}
