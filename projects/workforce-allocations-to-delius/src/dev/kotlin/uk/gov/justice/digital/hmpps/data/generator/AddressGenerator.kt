package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonAddress
import java.time.LocalDate

object AddressGenerator {

    val DEFAULT = generate(
        "Flame Cottage",
        null,
        "Mantle Place",
        "Hearth",
        postcode = "HE4 7TH"
    )

    fun generate(
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        personId: Long = PersonGenerator.DEFAULT.id,
        type: ReferenceData = ReferenceDataGenerator.ADDRESS_TYPE,
        status: ReferenceData = ReferenceDataGenerator.ADDRESS_STATUS_MAIN,
        startDate: LocalDate = LocalDate.now(),
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        id,
        personId,
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
}
