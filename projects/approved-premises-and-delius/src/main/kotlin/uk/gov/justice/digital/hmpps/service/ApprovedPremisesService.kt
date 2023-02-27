package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.getApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlert
import uk.gov.justice.digital.hmpps.integrations.delius.contact.alert.ContactAlertRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_ASSESSED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.ARRIVED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.BOOKING_MADE
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.DEPARTED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.NOT_ARRIVED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.Nsi.Companion.EXT_REF_BOOKING_PREFIX
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
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.getActiveManager
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.approvedPremisesAddressType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.mainAddressStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.previousAddressStatus
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
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val nsiRepository: NsiRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiManagerRepository: NsiManagerRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {
    @Transactional
    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        createContact(
            ContactDetails(date = details.submittedAt, type = APPLICATION_SUBMITTED),
            person = personRepository.getByCrn(event.crn()),
            staff = staffRepository.getByCode(details.submittedBy.staffMember.staffCode),
            probationAreaCode = details.submittedBy.probationArea.code
        )
    }

    @Transactional
    fun applicationAssessed(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationAssessedDetails(event.url()).eventDetails
        createContact(
            ContactDetails(
                date = details.assessedAt,
                type = APPLICATION_ASSESSED,
                notes = details.decisionRationale,
                description = "Approved Premises Application ${details.decision}"
            ),
            person = personRepository.getByCrn(event.crn()),
            staff = staffRepository.getByCode(details.assessedBy.staffMember.staffCode),
            probationAreaCode = details.assessedBy.probationArea.code
        )
    }

    @Transactional
    fun bookingMade(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingMadeDetails(event.url()).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        createContact(
            ContactDetails(
                date = details.createdAt,
                type = BOOKING_MADE,
                notes = "To view details of the Approved Premises booking, click here: ${details.applicationUrl}",
                description = "Approved Premises Booking for ${details.premises.name}"
            ),
            person = personRepository.getByCrn(event.crn()),
            staff = staffRepository.getByCode(details.bookedBy.staffMember.staffCode),
            probationAreaCode = ap.probationArea.code
        )
    }

    @Transactional
    fun personNotArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonNotArrivedDetails(event.url())
        val ap = approvedPremisesRepository.getApprovedPremises(details.eventDetails.premises.legacyApCode)
        createContact(
            ContactDetails(
                date = details.timestamp,
                type = NOT_ARRIVED,
                notes = listOfNotNull(
                    details.eventDetails.notes,
                    "For more details, click here: ${details.eventDetails.applicationUrl}"
                ).joinToString("\n\n")
            ),
            person = personRepository.getByCrn(event.crn()),
            staff = staffRepository.getByCode(details.eventDetails.recordedBy.staffCode),
            probationAreaCode = ap.probationArea.code
        )
    }

    @Transactional
    fun personArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonArrivedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val staff = staffRepository.getByCode(details.keyWorker.staffCode)
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        createContact(
            ContactDetails(
                date = details.arrivedAt,
                type = ARRIVED,
                notes = listOfNotNull(
                    details.notes,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString("\n\n")
            ),
            person = person,
            staff = staff,
            probationAreaCode = ap.probationArea.code
        )
        createResidenceNsi(
            person = person,
            staff = staff,
            details
        )
        updateMainAddress(person, details, ap)
    }

    @Transactional
    fun personDeparted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonDepartedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val staff = staffRepository.getByCode(details.keyWorker.staffCode)
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        createContact(
            ContactDetails(
                date = details.departedAt,
                type = DEPARTED,
                notes = "For details, see the referral on the AP Service: ${details.applicationUrl}",
                createAlert = false
            ),
            person = person,
            staff = staff,
            probationAreaCode = ap.probationArea.code
        )
        closeNsi(details)
        endMainAddress(person, details.departedAt.toLocalDate())
    }

    private fun closeNsi(details: PersonDeparted) {
        val nsi = nsiRepository.findByExternalReference(EXT_REF_BOOKING_PREFIX + details.bookingId)
        nsi?.actualEndDate = details.departedAt
    }

    private fun createContact(
        details: ContactDetails,
        person: Person,
        staff: Staff,
        probationAreaCode: String
    ) {
        val team = teamRepository.getUnallocatedTeam(probationAreaCode)
        val personManager = personManagerRepository.getActiveManager(person.id)
        val contact = contactRepository.save(
            Contact(
                date = details.date,
                type = contactTypeRepository.getByCode(details.type.code),
                description = details.description,
                person = person,
                staff = staff,
                team = team,
                notes = details.notes,
                alert = details.createAlert
            )
        )
        if (details.createAlert) {
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
    }

    private fun createResidenceNsi(
        person: Person,
        staff: Staff,
        details: PersonArrived
    ) {
        val nsi = nsiRepository.save(
            Nsi(
                person = person,
                type = nsiTypeRepository.getByCode(NsiTypeCode.APPROVED_PREMISES_RESIDENCE.code),
                status = nsiStatusRepository.getByCode(NsiStatusCode.IN_RESIDENCE.code),
                referralDate = details.applicationSubmittedOn,
                expectedStartDate = details.arrivedAt,
                actualStartDate = details.arrivedAt,
                expectedEndDate = details.expectedDepartureOn,
                notes = listOfNotNull(
                    details.notes,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString("\n\n"),
                externalReference = EXT_REF_BOOKING_PREFIX + details.bookingId
            )
        )
        val team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode)
        nsiManagerRepository.save(
            NsiManager(
                nsi = nsi,
                staff = staff,
                team = team,
                probationArea = team.probationArea,
                startDate = nsi.referralDate,
                transferReason = transferReasonRepository.getNsiTransferReason()
            )
        )
    }

    private fun updateMainAddress(person: Person, details: PersonArrived, ap: ApprovedPremises) {
        endMainAddress(person, details.arrivedAt.toLocalDate())
        ap.arrival(person, details).apply(personAddressRepository::save)
    }

    private fun endMainAddress(person: Person, endDate: LocalDate) {
        val currentMain = personAddressRepository.findMainAddress(person.id)
        currentMain?.apply {
            val previousStatus = referenceDataRepository.previousAddressStatus()
            currentMain.status = previousStatus
            currentMain.endDate = endDate
        }
    }

    private fun ApprovedPremises.arrival(person: Person, details: PersonArrived) = PersonAddress(
        0,
        person.id,
        referenceDataRepository.approvedPremisesAddressType(),
        referenceDataRepository.mainAddressStatus(),
        details.premises.name,
        address.addressNumber,
        address.streetName,
        address.district,
        address.town,
        address.county,
        address.postcode,
        address.telephoneNumber,
        startDate = details.arrivedAt.toLocalDate()
    )

    data class ContactDetails(
        val date: ZonedDateTime,
        val type: ContactTypeCode,
        val notes: String? = null,
        val description: String? = null,
        val createAlert: Boolean = true
    )
}
