package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.manager.Manager
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse
import java.time.ZonedDateTime

@Service
class ContactService(
    private val contactTypeRepository: ContactTypeRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactAlertRepository: ContactAlertRepository
) {
    fun createContact(
        detail: ContactDetail,
        person: Person,
        event: Event? = null,
        manager: Manager? = null,
        licenceConditionId: Long? = null
    ) {
        val cm = lazy { personManagerRepository.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(person.id) }
        val contact = contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(detail.typeCode.value),
                date = detail.date,
                startTime = detail.date,
                person = person,
                event = event,
                licenceConditionId = licenceConditionId,
                notes = detail.notes,
                staffId = manager?.staffId ?: cm.value.staff.id,
                teamId = manager?.teamId ?: cm.value.team.id,
                createdDatetime = detail.createdDateTimeOverride ?: ZonedDateTime.now(),
                alert = detail.alert
            )
        )
        if (detail.alert) {
            contactAlertRepository.save(
                ContactAlert(
                    contactId = contact.id,
                    typeId = contact.type.id,
                    personId = person.id,
                    personManagerId = cm.value.id,
                    staffId = cm.value.staff.id,
                    teamId = cm.value.team.id
                )
            )
        }
    }

    fun deleteFutureDatedLicenceConditionContacts(id: Long, terminationDate: ZonedDateTime) =
        contactRepository.deleteAllByLicenceConditionIdAndDateAfterAndOutcomeIdIsNull(id, terminationDate)
}

data class ContactDetail(
    val typeCode: ContactType.Code,
    val date: ZonedDateTime,
    val notes: String,
    val alert: Boolean = false,
    val createdDateTimeOverride: ZonedDateTime? = null
)
