package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.config.FeatureFlagName
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.*
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

@Service
class ApprovedPremisesService(
    private val detailService: DomainEventDetailService,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val staffRepository: StaffRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val contactService: ContactService,
    private val nsiService: NsiService,
    private val referralService: ReferralService,
    private val notifier: Notifier,
    private val featureFlags: FeatureFlags,
) {
    fun applicationSubmitted(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<ApplicationSubmitted>>(event).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val dEvent = eventRepository.getEvent(person.id, details.eventNumber)
        contactService.createContact(
            ContactDetails(
                date = details.submittedAt,
                typeCode = APPLICATION_SUBMITTED.code,
                description = "Approved Premises Application Submitted",
                notes = details.notes,
                externalReference = details.externalReference(),
            ),
            person = person,
            eventId = dEvent.id,
            staff = staffRepository.getByCode(details.submittedBy.staffMember.staffCode),
            probationAreaCode = details.submittedBy.probationArea.code
        )
    }

    fun applicationAssessed(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<ApplicationAssessed>>(event).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val dEvent = eventRepository.getEvent(person.id, details.eventNumber)
        contactService.createContact(
            ContactDetails(
                date = details.assessedAt,
                typeCode = APPLICATION_ASSESSED.code,
                notes = details.notes,
                description = "Approved Premises Application ${details.decision}",
                externalReference = details.externalReference()
            ),
            person = person,
            eventId = dEvent.id,
            staff = staffRepository.getByCode(details.assessedBy.staffMember.staffCode),
            probationAreaCode = details.assessedBy.probationArea.code
        )
    }

    fun applicationWithdrawn(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<ApplicationWithdrawn>>(event).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val dEvent = eventRepository.getEvent(person.id, details.eventNumber)
        contactService.createContact(
            ContactDetails(
                date = details.withdrawnAt,
                typeCode = APPLICATION_WITHDRAWN.code,
                notes = listOfNotNull(
                    details.withdrawalReason,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator()),
                externalReference = details.externalReference()
            ),
            person = person,
            eventId = dEvent.id,
            staff = staffRepository.getByCode(details.withdrawnBy.staffMember.staffCode),
            probationAreaCode = details.withdrawnBy.probationArea.code
        )
    }

    fun bookingMade(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<BookingMade>>(event).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        val person = personRepository.getByCrn(event.crn())
        referralService.bookingMade(person, details, ap)
        if (featureFlags.enabled(FeatureFlagName.PRE_ARRIVAL_NSI)) {
            nsiService.preArrival(ap, person, details)
        }
    }

    fun bookingChanged(event: HmppsDomainEvent) {
        val detailWrapper = detailService.getDetail<EventDetails<BookingChanged>>(event)
        val details = detailWrapper.eventDetails.apply {
            domainEventId = detailWrapper.id
        }
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        referralService.bookingChanged(event.crn(), details, ap)
    }

    fun bookingCancelled(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<BookingCancelled>>(event).eventDetails
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        referralService.bookingCancelled(event.crn(), details, ap)
    }

    fun personNotArrived(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<PersonNotArrived>>(event)
        val ap = approvedPremisesRepository.getApprovedPremises(details.eventDetails.premises.legacyApCode)
        referralService.personNotArrived(
            personRepository.getByCrn(event.crn()),
            ap,
            details.timestamp,
            details.eventDetails
        )
    }

    fun personArrived(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<PersonArrived>>(event).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        nsiService.personArrived(person, details, ap)?.let { (previousAddress, newAddress) ->
            notifier.addressCreated(person.crn, newAddress.id!!, newAddress.status.description)
            previousAddress?.let { notifier.addressUpdated(person.crn, it.id!!, it.status.description) }
        }
    }

    fun personDeparted(event: HmppsDomainEvent) {
        val details = detailService.getDetail<EventDetails<PersonDeparted>>(event).eventDetails
        val person = personRepository.getByCrn(event.crn())
        val ap = approvedPremisesRepository.getApprovedPremises(details.premises.legacyApCode)
        nsiService.personDeparted(person, details, ap)?.let { updatedAddress ->
            notifier.addressUpdated(person.crn, updatedAddress.id!!, updatedAddress.status.description)
        }
    }
}
