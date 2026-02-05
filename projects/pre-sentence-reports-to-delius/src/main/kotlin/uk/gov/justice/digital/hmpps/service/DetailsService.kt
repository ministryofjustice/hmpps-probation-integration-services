package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.entity.name
import uk.gov.justice.digital.hmpps.model.Address
import uk.gov.justice.digital.hmpps.model.DefendantDetails

@Service
class DetailsService
    (
    val documentRepository: DocumentRepository,
    val personAddressRepository: PersonAddressRepository
) {
    fun getDefendantDetails(psrUuid: String): DefendantDetails {
        val document = documentRepository.getbyUuid(psrUuid)
        val person = document.person
        val event = document.courtReport.courtAppearance.event
        val name = person.name()
        val address = personAddressRepository.findByPersonId(person.id)
            .filter() { it.status?.description == "CURRENT" }
            .map {
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
            eventNumber = event.eventNumber,
            name = name,
            dateOfBirth = person.dateOfBirth,
            mainAddress = address.first()
        )
    }
}