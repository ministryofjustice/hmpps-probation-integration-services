package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.cas3AddressType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.mainAddressStatus
import uk.gov.justice.digital.hmpps.integrations.delius.entity.previousAddressStatus
import java.time.LocalDate

@Service
class AddressService(
    private val personAddressRepository: PersonAddressRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun updateMainAddress(person: Person, details: PersonArrived) {
        endMainAddress(person, details.arrivedAt.toLocalDate())
        toPersonAddress(person, details).apply(personAddressRepository::save)
    }

    fun endMainAddress(person: Person, endDate: LocalDate) {
        val currentMain = personAddressRepository.findMainAddress(person.id)
        currentMain?.apply {
            val previousStatus = referenceDataRepository.previousAddressStatus()
            currentMain.status = previousStatus
            currentMain.endDate = endDate
        }
    }

    private fun toPersonAddress(person: Person, details: PersonArrived): PersonAddress {
        val addressLines = details.premises.addressLines
        return PersonAddress(
            0,
            person.id,
            referenceDataRepository.cas3AddressType(),
            referenceDataRepository.mainAddressStatus(),
            buildingName = addressLines.buildingName,
            streetName = addressLines.streetName,
            district = addressLines.district,
            town = details.premises.town,
            county = details.premises.region,
            postcode = details.premises.postcode,
            startDate = details.arrivedAt.toLocalDate()
        )
    }
}
