package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.Premises
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_ASSESSED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.ARRIVED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.BOOKING_MADE
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.NOT_ARRIVED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiManager
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.getNsiTransferReason
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.getActiveManager
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationAreaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getApprovedPremisesTeam
import uk.gov.justice.digital.hmpps.integrations.delius.team.getUnallocatedTeam
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url
import java.time.LocalDate
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
    private val probationAreaRepository: ProbationAreaRepository,
    private val nsiRepository: NsiRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiManagerRepository: NsiManagerRepository,
    private val transferReasonRepository: TransferReasonRepository,
) {
    @Transactional
    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        createAlertContact(
            person = personRepository.getByCrn(event.crn()),
            type = APPLICATION_SUBMITTED,
            date = details.submittedAt,
            staff = staffRepository.getByCode(details.submittedBy.staffMember.staffCode),
            probationAreaCode = details.submittedBy.probationArea.code
        )
    }

    @Transactional
    fun applicationAssessed(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationAssessedDetails(event.url()).eventDetails
        createAlertContact(
            person = personRepository.getByCrn(event.crn()),
            type = APPLICATION_ASSESSED,
            description = "Approved Premises Application ${details.decision}",
            notes = details.decisionRationale,
            date = details.assessedAt,
            staff = staffRepository.getByCode(details.assessedBy.staffMember.staffCode),
            probationAreaCode = details.assessedBy.probationArea.code,
        )
    }

    @Transactional
    fun bookingMade(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingMadeDetails(event.url()).eventDetails
        createAlertContact(
            person = personRepository.getByCrn(event.crn()),
            type = BOOKING_MADE,
            description = "Approved Premises Booking for ${details.premises.name}",
            notes = "To view details of the Approved Premises booking, click here: ${details.applicationUrl}",
            date = details.createdAt,
            staff = staffRepository.getByCode(details.bookedBy.staffMember.staffCode),
            probationAreaCode = details.premises.probationArea.code,
        )
    }

    @Transactional
    fun personNotArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonNotArrivedDetails(event.url())
        createAlertContact(
            person = personRepository.getByCrn(event.crn()),
            type = NOT_ARRIVED,
            notes = listOfNotNull(
                details.eventDetails.notes,
                "For more details, click here: ${details.eventDetails.applicationUrl}"
            ).joinToString("\n\n"),
            date = details.timestamp,
            staff = staffRepository.getByCode(details.eventDetails.recordedBy.staffCode),
            probationAreaCode = details.eventDetails.premises.probationArea.code,
        )
    }

    @Transactional
    fun personArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonArrivedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val staff = staffRepository.getByCode(details.keyWorker.staffCode)
        createAlertContact(
            person = person,
            type = ARRIVED,
            notes = listOfNotNull(
                details.notes,
                "For more details, click here: ${details.applicationUrl}"
            ).joinToString("\n\n"),
            date = details.arrivedAt,
            staff = staff,
            probationAreaCode = details.premises.probationArea.code,
        )
        createResidenceNsi(
            person = person,
            staff = staff,
            premises = details.premises,
            arrivalDate = details.arrivedAt,
            expectedDepartureDate = details.expectedDepartureOn,
            notes = listOfNotNull(
                details.notes,
                "For more details, click here: ${details.applicationUrl}"
            ).joinToString("\n\n"),
        )
        // updateMainAddress()
    }

    fun createAlertContact(
        date: ZonedDateTime,
        type: ContactTypeCode,
        person: Person,
        staff: Staff,
        probationAreaCode: String,
        description: String? = null,
        notes: String? = null,
    ) {
        val team = teamRepository.getUnallocatedTeam(probationAreaCode)
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

    private fun createResidenceNsi(
        person: Person,
        staff: Staff,
        premises: Premises,
        arrivalDate: ZonedDateTime,
        expectedDepartureDate: LocalDate?,
        notes: String,
    ) {
        val nsi = nsiRepository.save(
            Nsi(
                person = person,
                type = nsiTypeRepository.getByCode(NsiTypeCode.APPROVED_PREMISES_RESIDENCE.code),
                status = nsiStatusRepository.getByCode(NsiStatusCode.IN_RESIDENCE.code),
                referralDate = arrivalDate,
                expectedStartDate = arrivalDate,
                actualStartDate = arrivalDate,
                expectedEndDate = expectedDepartureDate,
                notes = notes,
            )
        )
        val team = teamRepository.getApprovedPremisesTeam(premises.legacyApCode)
        val probationArea = probationAreaRepository.getByCode(premises.probationArea.code)
        nsiManagerRepository.save(
            NsiManager(
                nsi = nsi,
                staff = staff,
                team = team,
                probationArea = probationArea,
                startDate = nsi.referralDate,
                transferReason = transferReasonRepository.getNsiTransferReason()
            )
        )
    }
}
