package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.ContactTypeRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import java.time.ZonedDateTime

@Service
class ContactService(
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
) {
    @Transactional
    fun createContact(
        crn: String,
        typeCode: String,
        occurredAt: ZonedDateTime,
        notes: String,
        externalReference: String,
    ): Contact {
        val person = personRepository.findByCrn(crn).orNotFoundBy("CRN", crn)
        return contactRepository.save(
            Contact(
                person = person,
                type = contactTypeRepository.getByCode(typeCode),
                date = occurredAt.toLocalDate(),
                startTime = occurredAt,
                notes = notes,
                externalReference = externalReference,
            )
        )
    }
}
