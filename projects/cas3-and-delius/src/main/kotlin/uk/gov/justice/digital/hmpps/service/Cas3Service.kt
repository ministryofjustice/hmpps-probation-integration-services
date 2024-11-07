package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.Cas3ApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url

@Service
@Transactional
class Cas3Service(
    private val contactService: ContactService,
    private val addressService: AddressService,
    private val cas3ApiClient: Cas3ApiClient,
    private val personRepository: PersonRepository
) {
    fun referralSubmitted(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            cas3ApiClient.getApplicationSubmittedDetails(event.url())
        }
    }

    fun bookingCancelled(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            cas3ApiClient.getBookingCancelledDetails(event.url())
        }
    }

    fun bookingConfirmed(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            cas3ApiClient.getBookingConfirmedDetails(event.url())
        }
    }

    fun bookingProvisionallyMade(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn()) {
            cas3ApiClient.getBookingProvisionallyMade(event.url())
        }
    }

    fun personArrived(event: HmppsDomainEvent): Pair<PersonAddress?, PersonAddress> {
        val person = personRepository.getByCrn(event.crn())
        val detail = cas3ApiClient.getPersonArrived(event.url())
        contactService.createOrUpdateContact(event.crn(), person) { detail }
        return addressService.updateMainAddress(person, detail.eventDetails)
    }

    fun personDeparted(event: HmppsDomainEvent): PersonAddress? {
        val person = personRepository.getByCrn(event.crn())
        val detail = cas3ApiClient.getPersonDeparted(event.url())
        contactService.createOrUpdateContact(event.crn(), person) { detail }
        return addressService.endMainCAS3Address(person, detail.eventDetails.departedAt.toLocalDate())
    }

    fun personArrivedUpdated(event: HmppsDomainEvent): PersonAddress? {
        val person = personRepository.getByCrn(event.crn())
        val detail = cas3ApiClient.getPersonArrived(event.url())
        contactService.createOrUpdateContact(
            event.crn(),
            replaceNotes = false,
            extraInfo = "Address details were updated: ${DeliusDateTimeFormatter.format(detail.timestamp)}"
        ) { detail }
        return addressService.updateCas3Address(person, detail.eventDetails)
    }

    fun personDepartedUpdated(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn(), replaceNotes = false) {
            cas3ApiClient.getPersonDeparted(event.url())
        }
    }

    fun bookingCancelledUpdated(event: HmppsDomainEvent) {
        contactService.createOrUpdateContact(event.crn(), replaceNotes = false) {
            cas3ApiClient.getBookingCancelledDetails(event.url())
        }
    }
}