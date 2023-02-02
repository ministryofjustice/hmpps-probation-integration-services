package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPerson
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewPersonAddress
import java.time.LocalDate

object AddressGenerator {

    val CASE_VIEW = forCaseView(
        "Flame Cottage",
        null,
        "Mantle Place",
        "Hearth",
        postcode = "HE4 7TH"
    )

    fun forCaseView(
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        person: CaseViewPerson = PersonGenerator.CASE_VIEW,
        type: ReferenceData = ReferenceDataGenerator.ADDRESS_TYPE,
        status: ReferenceData = ReferenceDataGenerator.ADDRESS_STATUS_MAIN,
        startDate: LocalDate = LocalDate.now(),
        id: Long = IdGenerator.getAndIncrement()
    ): CaseViewPersonAddress {
        val address = CaseViewPersonAddress(
            id,
            person.id,
            type,
            status,
            buildingName,
            addressNumber,
            streetName,
            town,
            county,
            postcode,
            startDate = startDate
        )
        return address
    }
}
