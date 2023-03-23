package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryAddress
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

object AddressGenerator {
    val CASE_SUMMARY_MAIN_ADDRESS = generate("", "123", "Fake Street")

    fun generate(
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        personId: Long = PersonGenerator.CASE_SUMMARY.id,
        status: ReferenceData = ReferenceData(IdGenerator.getAndIncrement(), "M", "Main"),
        id: Long = IdGenerator.getAndIncrement()
    ) = CaseSummaryAddress(id, personId, status, buildingName, addressNumber, streetName, town, county, postcode, false)
}
