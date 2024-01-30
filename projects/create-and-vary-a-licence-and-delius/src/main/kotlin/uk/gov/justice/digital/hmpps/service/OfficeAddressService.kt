package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.OfficeAddress
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.OfficeLocation
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.OfficeLocationRepository

@Service
class OfficeAddressService(private val officeLocationRepository: OfficeLocationRepository) {
    fun findAddresses(ldu: String, officeName: String, pageable: Pageable): Page<OfficeAddress> =
        officeLocationRepository.findByLduAndOfficeName(ldu, officeName, pageable).map {
            it.asAddress()
        }
}

fun OfficeLocation.asAddress() = OfficeAddress(
    description,
    buildingName,
    buildingNumber,
    streetName,
    district,
    townCity,
    county,
    postcode,
    ldu.description,
    telephoneNumber,
    startDate,
    endDate
)
