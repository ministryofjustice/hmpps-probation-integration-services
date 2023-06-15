package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.getApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.locationCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_ASSESSED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.APPLICATION_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.BOOKING_MADE
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.NOT_ARRIVED
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url

@Service
class ApprovedPremisesService(
    private val approvedPremisesApiClient: ApprovedPremisesApiClient,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val personRepository: PersonRepository,
    private val contactService: ContactService,
    private val nsiService: NsiService
) {
    @Transactional
    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        contactService.createContact(
            ContactDetails(date = details.submittedAt, type = APPLICATION_SUBMITTED),
            person = personRepository.getByCrn(event.crn()),
            staffCode = details.submittedBy.staffMember.staffCode,
            probationAreaCode = details.submittedBy.probationArea.code
        )
    }

    @Transactional
    fun applicationAssessed(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationAssessedDetails(event.url()).eventDetails
        contactService.createContact(
            ContactDetails(
                date = details.assessedAt,
                type = APPLICATION_ASSESSED,
                notes = details.decisionRationale,
                description = "Approved Premises Application ${details.decision}"
            ),
            person = personRepository.getByCrn(event.crn()),
            staffCode = details.assessedBy.staffMember.staffCode,
            probationAreaCode = details.assessedBy.probationArea.code
        )
    }

    @Transactional
    fun bookingMade(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingMadeDetails(event.url()).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        contactService.createContact(
            ContactDetails(
                date = details.createdAt,
                type = BOOKING_MADE,
                notes = "To view details of the Approved Premises booking, click here: ${details.applicationUrl}",
                description = "Approved Premises Booking for ${details.premises.name}",
                locationCode = ap.locationCode()
            ),
            person = personRepository.getByCrn(event.crn()),
            staffCode = details.bookedBy.staffMember.staffCode,
            probationAreaCode = ap.probationArea.code
        )
    }

    @Transactional
    fun personNotArrived(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonNotArrivedDetails(event.url())
        val ap = approvedPremisesRepository.getApprovedPremises(details.eventDetails.premises.legacyApCode)
        contactService.createContact(
            ContactDetails(
                date = details.timestamp,
                type = NOT_ARRIVED,
                locationCode = ap.locationCode(),
                notes = listOfNotNull(
                    details.eventDetails.notes,
                    "For more details, click here: ${details.eventDetails.applicationUrl}"
                ).joinToString("\n\n")
            ),
            person = personRepository.getByCrn(event.crn()),
            staffCode = details.eventDetails.recordedBy.staffCode,
            probationAreaCode = ap.probationArea.code
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
