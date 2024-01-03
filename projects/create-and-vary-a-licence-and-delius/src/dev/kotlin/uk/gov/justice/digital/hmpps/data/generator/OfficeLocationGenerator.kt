package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.OfficeAddress
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.OfficeLocation
import java.time.LocalDate

object OfficeLocationGenerator {
    val DISTRICT_BRK = generateDistrict("TVP_BRK","Berkshire")
    val DISTRICT_OXF = generateDistrict("TVP_OXF","Oxfordshire")
    val DISTRICT_MKY = generateDistrict("TVP_MKY","Milton Keynes")

    val LOCATION_BRK_1 = generateLocation(
        code = "TVP_BRK",
        description = "Bracknell Office",
        buildingNumber = "21",
        streetName = "Some Place",
        town = "Hearth",
        postcode = "H34 7TH",
        district = DISTRICT_BRK
    )

    val LOCATION_BRK_2 = generateLocation(
        code = "TVP_RCC",
        description = "Reading Office",
        buildingNumber = "23",
        buildingName = "The old hall",
        streetName = "Another Place",
        town = "Reading",
        postcode = "RG1 3EH",
        district = DISTRICT_BRK
    )

    fun generateDistrict(
        code: String,
        description: String,
        borough: Borough = ProviderGenerator.DEFAULT_BOROUGH,
        id: Long = IdGenerator.getAndIncrement()
    ) = District(code, description, borough, id)

    fun generateOfficeAddress(
        officeLocation: OfficeLocation,
        officeDistrict: District) = OfficeAddress(
        officeLocation.description,
        officeLocation.buildingName,
        officeLocation.buildingNumber,
        officeLocation.streetName,
        officeDistrict.description,
        officeLocation.townCity,
        officeLocation.county,
        officeLocation.postcode,
        officeLocation.telephoneNumber,
        officeLocation.startDate,
        officeLocation.endDate)

    fun generateLocation(location: OfficeLocation, district: District) =
        OfficeLocation(
            location.code,
            location.description,
            location.buildingName,
            location.buildingNumber,
            location.streetName,
            location.districtStr,
            location.townCity,
            location.county,
            location.postcode,
            location.telephoneNumber,
            location.startDate,
            location.endDate,
            district,
            location.id)

    fun generateLocation(
        code: String,
        description: String,
        buildingName: String? = null,
        buildingNumber: String,
        streetName: String? = null,
        districtStr: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        district: District,
        id: Long = IdGenerator.getAndIncrement()
    ) = OfficeLocation(
        code,
        description,
        buildingName,
        buildingNumber,
        streetName,
        districtStr,
        town,
        county,
        postcode,
        telephoneNumber,
        startDate,
        endDate,
        district,
        id
    )
}
