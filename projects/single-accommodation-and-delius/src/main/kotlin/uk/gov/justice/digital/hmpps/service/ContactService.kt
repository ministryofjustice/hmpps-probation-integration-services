package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.DutyToRefer
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
    fun createContact(details: DutyToRefer, occurredAt: ZonedDateTime, typeCode: String): Contact {
        val person = personRepository.findByCrn(details.crn).orNotFoundBy("crn", details.crn)
        return contactRepository.save(
            Contact(
                person = person,
                type = contactTypeRepository.getByCode(typeCode),
                date = occurredAt.toLocalDate(),
                startTime = occurredAt,
                notes = "For more details, click here: ${details.viewUrl}",
                externalReference = "urn:uk:gov:hmpps:find-and-manage-accommodation:duty-to-refer:${details.id}",
            )
        )
    }
}
