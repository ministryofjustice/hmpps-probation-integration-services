package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.EventRepository
import uk.gov.justice.digital.hmpps.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.entity.findMainAddress
import uk.gov.justice.digital.hmpps.entity.name
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.Address
import uk.gov.justice.digital.hmpps.model.DefendantDetails

@Service
class DetailsService
    (
    private val documentRepository: DocumentRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val eventRepository: EventRepository
) {
    fun getDefendantDetails(psrUuid: String): DefendantDetails {
        val document = documentRepository.getByUuid(psrUuid)
        val person = document.person
        val eventId = documentRepository.getEventIdByUuid(psrUuid)
        val eventNumber = eventRepository.findById(eventId).get().eventNumber.toInt()
            .orNotFoundBy("CRN", person.crn)
        val name = person.name()
        val address = personAddressRepository.findMainAddress(person.id)?.let {
            Address(
                it.buildingName,
                it.buildingNumber,
                it.streetName,
                it.district,
                it.townCity,
                it.county,
                it.postcode,
                it.noFixedAbode
            )
        }

        return DefendantDetails(
            crn = person.crn,
            eventNumber = eventNumber,
            name = name,
            dateOfBirth = person.dateOfBirth,
            mainAddress = address
        )
    }
}
