package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.AddressEntity

object AddressGenerator {
    val DEFAULT = generate("", "1", "Promise Street", "", "Make Believe", "", "MB01 1PS", "01234567890")

    fun generate(
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = AddressEntity(id, buildingName, addressNumber, streetName, district, town, county, postcode, telephoneNumber)
}
