package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApprovedPremisesApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.getApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.getEvent
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.Notifier
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url

@Service
class ApprovedPremisesService(
    private val approvedPremisesApiClient: ApprovedPremisesApiClient,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val contactService: ContactService,
    private val nsiService: NsiService,
    private val referralService: ReferralService,
    private val notifier: Notifier,
) {
    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val dEvent = eventRepository.getEvent(person.id, details.eventNumber)
        contactService.createContact(
            ContactDetails(
                date = details.submittedAt,
                type = APPLICATION_SUBMITTED,
                description = "Approved Premises Application Submitted",
                notes = details.notes
            ),
            person = person,
            eventId = dEvent.id,
            staff = staffRepository.getByCode(details.submittedBy.staffMember.staffCode),
            probationAreaCode = details.submittedBy.probationArea.code
        )
    }

    fun applicationAssessed(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationAssessedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val dEvent = eventRepository.getEvent(person.id, details.eventNumber)
        contactService.createContact(
            ContactDetails(
                date = details.assessedAt,
                type = APPLICATION_ASSESSED,
                notes = details.notes,
                description = "Approved Premises Application ${details.decision}"
            ),
            person = person,
            eventId = dEvent.id,
            staff = staffRepository.getByCode(details.assessedBy.staffMember.staffCode),
            probationAreaCode = details.assessedBy.probationArea.code
        )
    }

    fun applicationWithdrawn(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getApplicationWithdrawnDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val dEvent = eventRepository.getEvent(person.id, details.eventNumber)
        contactService.createContact(
            ContactDetails(
                date = details.withdrawnAt,
                type = APPLICATION_WITHDRAWN,
                notes = listOfNotNull(
                    details.withdrawalReason,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator())
            ),
            person = person,
            eventId = dEvent.id,
            staff = staffRepository.getByCode(details.withdrawnBy.staffMember.staffCode),
            probationAreaCode = details.withdrawnBy.probationArea.code
        )
    }

    fun bookingMade(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingMadeDetails(event.url()).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        referralService.bookingMade(event.crn(), details, ap)
    }

    fun bookingChanged(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingChangedDetails(event.url()).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        referralService.bookingChanged(event.crn(), details, ap)
    }

    fun bookingCancelled(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getBookingCancelledDetails(event.url()).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        referralService.bookingCancelled(event.crn(), details, ap)
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
        nsiService.personArrived(person, details, ap)?.let { (previousAddress, newAddress) ->
            notifier.addressCreated(person.crn, newAddress.id!!, newAddress.status.description)
            previousAddress?.let { notifier.addressUpdated(person.crn, it.id!!, it.status.description) }
        }
    }

    fun personDeparted(event: HmppsDomainEvent) {
        val details = approvedPremisesApiClient.getPersonDepartedDetails(event.url()).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        nsiService.personDeparted(person, details, ap)?.let { updatedAddress ->
            notifier.addressUpdated(person.crn, updatedAddress.id!!, updatedAddress.status.description)
        }
    }
}
