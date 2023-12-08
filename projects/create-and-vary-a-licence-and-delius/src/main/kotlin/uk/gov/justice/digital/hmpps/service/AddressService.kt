package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddressRepository

@Service
class AddressService(private val personAddressRepository: PersonAddressRepository) {
    fun findAddresses(crn: String): List<Address> =
        personAddressRepository.findAllByPersonCrnOrderByStartDateDesc(crn).mapNotNull {
            Address.from(
                it.buildingName,
                it.buildingNumber,
                it.streetName,
                it.district,
                it.town,
                it.county,
                it.postcode,
                it.startDate,
                it.endDate,
            )
        }
}
