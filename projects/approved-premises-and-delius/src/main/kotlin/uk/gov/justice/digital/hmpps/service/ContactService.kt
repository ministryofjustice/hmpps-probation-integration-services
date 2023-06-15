package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.location.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.getActiveManager
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getUnallocatedTeam
import java.time.ZonedDateTime

@Service
class ContactService(
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val personManagerRepository: PersonManagerRepository
) {
    fun createContact(
        details: ContactDetails,
        person: Person,
        staffCode: String,
        probationAreaCode: String
    ): Contact {
        return contactRepository.findByPersonIdAndTypeCodeAndStartTime(person.id, details.type.code, details.date)
            ?: run {
                val team = teamRepository.getUnallocatedTeam(probationAreaCode)
                val staff = staffRepository.getByCode(staffCode)
                val contact = contactRepository.save(
                    Contact(
                        date = details.date.toLocalDate(),
                        startTime = details.date,
                        type = contactTypeRepository.getByCode(details.type.code),
                        outcome = details.outcomeCode?.let { contactOutcomeRepository.findByCode(it) },
                        locationId = details.locationCode?.let { officeLocationRepository.findByCode(it) }?.id,
                        description = details.description,
                        person = person,
                        staff = staff,
                        team = team,
                        notes = details.notes,
                        alert = details.createAlert
                    )
                )
                if (details.createAlert) {
                    val personManager = personManagerRepository.getActiveManager(person.id)
                    contactAlertRepository.save(
                        ContactAlert(
                            contactId = contact.id,
                            typeId = contact.type.id,
                            personId = person.id,
                            personManagerId = personManager.id,
                            staffId = personManager.staff.id,
                            teamId = personManager.team.id
                        )
                    )
                }
                return contact
            }
    }
}

data class ContactDetails(
    val date: ZonedDateTime,
    val type: ContactTypeCode,
    val outcomeCode: String? = null,
    val locationCode: String? = null,
    val notes: String? = null,
    val description: String? = null,
    val createAlert: Boolean = true
)
