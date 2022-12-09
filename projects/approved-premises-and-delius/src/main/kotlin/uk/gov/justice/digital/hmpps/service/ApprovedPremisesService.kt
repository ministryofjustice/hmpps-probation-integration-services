package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.getActiveManager
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getUnallocatedTeam
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.net.URI
import java.time.ZonedDateTime

@Service
class ApprovedPremisesService(
    private val approvedPremisesApiClient: ApprovedPremisesApiClient,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
) {
    @Transactional
    fun applicationSubmitted(event: HmppsDomainEvent) {
        val detailUrl = URI.create(event.detailUrl ?: throw IllegalArgumentException("Missing detail url"))
        val details = approvedPremisesApiClient.getApplicationSubmittedDetails(detailUrl).eventDetails
        createAlertContact(
            crn = event.personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN"),
            type = APPLICATION_SUBMITTED,
            date = details.submittedAt,
            staffCode = details.submittedBy.staffCode,
            probationAreaCode = details.probationArea.code
        )
    }

    fun createAlertContact(date: ZonedDateTime, type: ContactTypeCode, crn: String, staffCode: String, probationAreaCode: String) {
        val staff = staffRepository.getByCode(staffCode)
        val team = teamRepository.getUnallocatedTeam(probationAreaCode)
        val person = personRepository.getByCrn(crn)
        val personManager = personManagerRepository.getActiveManager(person.id)
        val contact = contactRepository.save(
            Contact(
                date = date,
                startTime = date,
                type = contactTypeRepository.getByCode(APPLICATION_SUBMITTED.code),
                person = person,
                staff = staff,
                team = team,
                alert = true
            )
        )
        contactAlertRepository.save(
            ContactAlert(
                contactId = contact.id,
                typeId = contact.type.id,
                personId = person.id,
                personManagerId = personManager.id,
                staffId = personManager.staff.id,
                teamId = personManager.team.id,
            )
        )
    }
}
