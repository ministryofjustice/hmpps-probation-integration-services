package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Address
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

object AddressGenerator {
    val MAIN_STATUS = ReferenceData(IdGenerator.getAndIncrement(), "M", "Main")
    val POSTAL_STATUS = ReferenceData(IdGenerator.getAndIncrement(), "P", "Postal")

    val MAIN_ADDRESS = generate(
        personId = PersonGenerator.DEFAULT.id,
        status = MAIN_STATUS,
        buildingNumber = "2789",
        streetName = "Main Street",
        townCity = "Maintown",
        district = "MainDistrict",
        county = "Maincounty",
        postcode = "MA30 3IN",
    )

    val POSTAL_ADDRESS = generate(
        personId = PersonGenerator.DEFAULT.id,
        status = POSTAL_STATUS,
        buildingNumber = "281",
        streetName = "Postal Default Street",
        townCity = "Postinton",
        district = "Postrict",
        county = "County Post",
        postcode = "NE30 3ZZ",
    )

    val END_DATED_ADDRESS = generate(
        personId = PersonGenerator.DEFAULT.id,
        status = MAIN_STATUS,
        buildingNumber = "1",
        streetName = "Old Street",
        townCity = "Old Town",
        postcode = "OL1 1AA",
        endDate = LocalDate.now().minusDays(1),
    )

    fun generate(
        personId: Long,
        status: ReferenceData,
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        townCity: String? = null,
        district: String? = null,
        county: String? = null,
        postcode: String? = null,
        startDate: LocalDate = LocalDate.now().minusMonths(6),
        endDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Address(
        id = id,
        personId = personId,
        status = status,
        buildingName = buildingName,
        buildingNumber = buildingNumber,
        streetName = streetName,
        townCity = townCity,
        district = district,
        county = county,
        postcode = postcode,
        startDate = startDate,
        endDate = endDate,
    )
}
