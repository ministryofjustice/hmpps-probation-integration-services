package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_ASSESSED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.BOOKING_MADE
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.NOT_ARRIVED
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
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url
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
        val details = approvedPremisesApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        createAlertContact(
            crn = event.crn(),
            type = APPLICATION_SUBMITTED,
            date = details.submittedAt,
            staffCode = details.submittedBy.staffCode,
            probationAreaCode = details.probationArea.code
        )
    }

    @Transactional
    fun applicationAssessed(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationAssessedDetails(event.url()).eventDetails
        createAlertContact(
            crn = event.crn(),
            type = APPLICATION_ASSESSED,
            description = "Approved Premises Application ${details.decision}",
            notes = details.decisionRationale,
            date = details.assessedAt,
            staffCode = details.assessedBy.staffCode,
            probationAreaCode = details.assessmentArea.code,
        )
    }

    @Transactional
    fun bookingMade(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingMadeDetails(event.url()).eventDetails
        createAlertContact(
            crn = event.crn(),
            type = BOOKING_MADE,
            description = "Approved Premises Booking for ${details.premises.name}",
            notes = "To view details of the Approved Premises booking, click here: ${details.applicationUrl}",
            date = details.createdAt,
            staffCode = details.bookedBy.staffCode,
            probationAreaCode = details.premises.probationArea.code,
        )
    }

    @Transactional
    fun personNotArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonNotArrivedDetails(event.url())
        createAlertContact(
            crn = event.crn(),
            type = NOT_ARRIVED,
            notes = listOfNotNull(
                details.eventDetails.notes,
                "For more details, click here: ${details.eventDetails.applicationUrl}"
            ).joinToString("\n\n"),
            date = details.timestamp,
            staffCode = details.eventDetails.recordedBy.staffCode,
            probationAreaCode = details.eventDetails.premises.probationArea.code,
        )
    }

    fun createAlertContact(
        date: ZonedDateTime,
        type: ContactTypeCode,
        crn: String,
        staffCode: String,
        probationAreaCode: String,
        description: String? = null,
        notes: String? = null,
    ) {
        val staff = staffRepository.getByCode(staffCode)
        val team = teamRepository.getUnallocatedTeam(probationAreaCode)
        val person = personRepository.getByCrn(crn)
        val personManager = personManagerRepository.getActiveManager(person.id)
        val contact = contactRepository.save(
            Contact(
                date = date,
                startTime = date,
                type = contactTypeRepository.getByCode(type.code),
                description = description,
                person = person,
                staff = staff,
                team = team,
                notes = notes,
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
