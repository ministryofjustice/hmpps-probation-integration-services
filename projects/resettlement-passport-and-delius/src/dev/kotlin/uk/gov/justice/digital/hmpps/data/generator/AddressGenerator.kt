package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.PersonAddress

object AddressGenerator {
    val DEFAULT = generate(null, "11", "Castle Street", "My town", "Magic Land", "Hoth", "ML01 1CS")

    fun generate(
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        district: String? = null,
        county: String? = null,
        postcode: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        id,
        PersonGenerator.DEFAULT,
        status = ReferenceDataGenerator.ADDRESS_STATUS,
        buildingName,
        addressNumber,
        streetName,
        district,
        town,
        county,
        postcode
    )
}
