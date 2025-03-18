package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Address
import uk.gov.justice.digital.hmpps.entity.ReferenceData

object AddressGenerator {
    val MAIN_STATUS = ReferenceData(IdGenerator.getAndIncrement(), "M")
    val DEFAULT = Address(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT.id,
        status = MAIN_STATUS,
        buildingName = "Building name",
        addressNumber = "123",
        streetName = "Street",
        townCity = "Town",
        district = "District",
        county = "County",
        postcode = "POSTCODE",
        noFixedAbode = false
    )
}
