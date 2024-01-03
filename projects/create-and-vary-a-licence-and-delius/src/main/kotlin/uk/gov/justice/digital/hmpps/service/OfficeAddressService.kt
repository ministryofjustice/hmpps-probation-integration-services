package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.OfficeAddress
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.OfficeLocationRepository

@Service
class OfficeAddressService(private val officeLocationRepository: OfficeLocationRepository) {
    fun findAddresses(ldu: String, officeName: String, pageable: Pageable): Page<OfficeAddress> =
        officeLocationRepository.findByLduAndOfficeName(ldu, officeName, pageable).map {
            OfficeAddress.from(
                it.description,
                it.buildingName,
                it.buildingNumber,
                it.streetName,
                it.district.description,
                it.townCity,
                it.county,
                it.postcode,
                it.telephoneNumber,
                it.startDate,
                it.endDate
            )
        }
}
