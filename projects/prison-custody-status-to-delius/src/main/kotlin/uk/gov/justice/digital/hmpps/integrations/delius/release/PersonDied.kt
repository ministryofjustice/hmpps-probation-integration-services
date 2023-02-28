package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse
import java.time.ZonedDateTime

@Service
class PersonDied(
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val contactAlertRepository: ContactAlertRepository
) {
    fun inCustody(nomsId: String, dateTime: ZonedDateTime) {
        val people = personRepository.findByNomsNumberAndSoftDeletedIsFalse(nomsId)
        val person = when (people.size) {
            1 -> people[0]
            0 -> throw NotFoundException("Person", "nomsNumber", nomsId)
            else -> throw IllegalStateException("More than one case with the same Noms Number: $nomsId")
        }
        createAlertContact(person, dateTime)
    }

    private fun createAlertContact(
        person: Person,
        dateTime: ZonedDateTime
    ) {
        val cm = personManagerRepository.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id)
        val contact = contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(ContactTypeCode.DIED_IN_CUSTODY.code),
                date = dateTime,
                person = person,
                notes = "This information has been provided via a movement reason recorded at ${
                DeliusDateTimeFormatter.format(dateTime)
                } in NOMIS",
                staffId = cm.staff.id,
                teamId = cm.team.id,
                alert = true
            )
        )
        contactAlertRepository.save(
            ContactAlert(
                contactId = contact.id,
                typeId = contact.type.id,
                personId = person.id,
                personManagerId = cm.id,
                staffId = cm.staff.id,
                teamId = cm.team.id
            )
        )
    }
}
