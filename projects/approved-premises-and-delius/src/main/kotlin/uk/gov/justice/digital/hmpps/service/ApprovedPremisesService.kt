package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.getApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_ASSESSED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url

@Service
class ApprovedPremisesService(
    private val approvedPremisesApiClient: ApprovedPremisesApiClient,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val contactService: ContactService,
    private val nsiService: NsiService,
    private val referralService: ReferralService
) {
    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        contactService.createContact(
            ContactDetails(
                date = details.submittedAt,
                type = APPLICATION_SUBMITTED,
                description = "Approved Premises Application Submitted",
                notes = details.notes
            ),
            person = personRepository.getByCrn(event.crn()),
            staff = staffRepository.getByCode(details.submittedBy.staffMember.staffCode),
            probationAreaCode = details.submittedBy.probationArea.code
        )
    }

    fun applicationAssessed(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationAssessedDetails(event.url()).eventDetails
        contactService.createContact(
            ContactDetails(
                date = details.assessedAt,
                type = APPLICATION_ASSESSED,
                notes = details.notes,
                description = "Approved Premises Application ${details.decision}"
            ),
            person = personRepository.getByCrn(event.crn()),
            staff = staffRepository.getByCode(details.assessedBy.staffMember.staffCode),
            probationAreaCode = details.assessedBy.probationArea.code
        )
    }

    fun bookingMade(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingMadeDetails(event.url()).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        referralService.bookingMade(event.crn(), details, ap)
    }

    fun personNotArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonNotArrivedDetails(event.url())
        val ap = approvedPremisesRepository.getApprovedPremises(details.eventDetails.premises.legacyApCode)
        referralService.personNotArrived(
            personRepository.getByCrn(event.crn()),
            ap,
            details.timestamp,
            details.eventDetails
        )
    }

    fun personArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonArrivedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        nsiService.personArrived(person, details, ap)
    }

    fun personDeparted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonDepartedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        nsiService.personDeparted(person, details, ap)
    }
}
