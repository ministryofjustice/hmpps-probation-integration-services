package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.crn

@Service
@Transactional
class Cas3Service(
    private val contactService: ContactService,
    private val addressService: AddressService,
    private val detailService: DomainEventDetailService,
    private val personRepository: PersonRepository
) {
    fun referralSubmitted(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            detailService.getDetail<EventDetails<ApplicationSubmitted>>(event)
        }
    }

    fun bookingCancelled(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            detailService.getDetail<EventDetails<BookingCancelled>>(event)
        }
    }

    fun bookingConfirmed(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            detailService.getDetail<EventDetails<BookingConfirmed>>(event)
        }
    }

    fun bookingProvisionallyMade(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            detailService.getDetail<EventDetails<BookingProvisional>>(event)
        }
    }

    fun personArrived(event: HmppsDomainEvent): Pair<PersonAddress?, PersonAddress> {
        val person = personRepository.getByCrn(event.crn())
        val detail = detailService.getDetail<EventDetails<PersonArrived>>(event)
        contactService.createOrUpdateContact(event.crn(), person) { detail }
        return addressService.updateMainAddress(person, detail.eventDetails)
    }

    fun personDeparted(event: HmppsDomainEvent): PersonAddress? {
        val person = personRepository.getByCrn(event.crn())
        val detail = detailService.getDetail<EventDetails<PersonDeparted>>(event)
        contactService.createOrUpdateContact(event.crn(), person) { detail }
        return addressService.endMainCAS3Address(person, detail.eventDetails.departedAt.toLocalDate())
    }

    fun personArrivedUpdated(event: HmppsDomainEvent): PersonAddress? {
        val person = personRepository.getByCrn(event.crn())
        val detail = detailService.getDetail<EventDetails<PersonArrived>>(event)
        contactService.createOrUpdateContact(
            event.crn(),
            replaceNotes = false,
            extraInfo = "Address details were updated: ${DeliusDateTimeFormatter.format(detail.timestamp)}"
        ) { detail }
        return addressService.updateCas3Address(person, detail.eventDetails)
    }

    fun personDepartedUpdated(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn(), replaceNotes = false) {
            detailService.getDetail<EventDetails<PersonDeparted>>(event)
        }
    }

    fun bookingCancelledUpdated(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn(), replaceNotes = false) {
            detailService.getDetail<EventDetails<BookingCancelled>>(event)
        }
    }
}