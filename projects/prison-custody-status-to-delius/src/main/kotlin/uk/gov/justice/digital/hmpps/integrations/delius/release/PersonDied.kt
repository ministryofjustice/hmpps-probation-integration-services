package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import java.time.ZonedDateTime

@Service
class PersonDied(
    private val personRepository: PersonRepository,
    private val contactService: ContactService
) {
    fun inCustody(nomsId: String, dateTime: ZonedDateTime) {
        val people = personRepository.findByNomsNumberAndSoftDeletedIsFalse(nomsId)
        val person = when (people.size) {
            1 -> people[0]
            0 -> throw IgnorableMessageException("MissingNomsNumber")
            else -> throw IgnorableMessageException("DuplicateNomsNumber")
        }
        val notes = "This information has been provided via a movement reason recorded at ${
        DeliusDateTimeFormatter.format(dateTime)
        } in NOMIS"
        contactService.createContact(
            ContactDetail(ContactType.Code.DIED_IN_CUSTODY, dateTime, notes, alert = true),
            person
        )
    }
}
