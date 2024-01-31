package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.OfficeLocation
import java.time.LocalDate

data class OfficeAddress(
    val officeName: String,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val ldu: String,
    val telephoneNumber: String?,
    val from: LocalDate,
    val to: LocalDate?
) {
    companion object {
        fun from(
            officeLocation: OfficeLocation
        ): OfficeAddress? =
            if (
                officeLocation.buildingName == null && officeLocation.buildingNumber == null && officeLocation.streetName == null &&
                officeLocation.district == null && officeLocation.townCity == null && officeLocation.county == null && officeLocation.postcode == null
            ) {
                null
            } else {
                OfficeAddress(
                    officeLocation.description,
                    officeLocation.buildingName,
                    officeLocation.buildingNumber,
                    officeLocation.streetName,
                    officeLocation.district,
                    officeLocation.townCity,
                    officeLocation.county,
                    officeLocation.postcode,
                    officeLocation.ldu.description,
                    officeLocation.telephoneNumber,
                    officeLocation.startDate,
                    officeLocation.endDate
                )
            }
    }
}
