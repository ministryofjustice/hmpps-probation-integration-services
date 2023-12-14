package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseAddress

object CaseAddressGenerator {
    val DEFAULT = generate("", "11", "Castle Street", "My town", "Magic Land", "Hoth", "ML01 1CS", "01234567890")

    fun generate(
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        district: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseAddress(
        id,
        CaseGenerator.DEFAULT,
        buildingName,
        addressNumber,
        streetName,
        district,
        town,
        county,
        postcode,
        telephoneNumber,
        status = ReferenceDataGenerator.MAIN_ADDRESS
    )
}
