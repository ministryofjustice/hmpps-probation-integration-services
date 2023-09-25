package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.AddressStatus
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import java.time.LocalDate

object AddressGenerator {
    val ADDRESS_STATUS_MAIN = generateStatus("M")
    val ADDRESS_STATUS_PREVIOUS = generateStatus("P")
    val ADDRESS_STATUS_OTHER = generateStatus("O")
    val ADDRESS_MAIN = generateAddress(
        PersonGenerator.DEFAULT_PERSON,
        ADDRESS_STATUS_MAIN,
        buildingNumber = "21",
        streetName = "Mantle Place",
        town = "Hearth",
        postcode = "H34 7TH"
    )
    val ADDRESS_PREVIOUS = generateAddress(
        PersonGenerator.DEFAULT_PERSON,
        ADDRESS_STATUS_PREVIOUS,
        buildingName = "Casa Anterior",
        streetName = "Plaza de Espana",
        county = "Seville",
        postcode = "S3 11E",
        startDate = LocalDate.now().minusDays(12),
        endDate = LocalDate.now().minusDays(1)
    )
    val ADDRESS_OTHER = generateAddress(PersonGenerator.DEFAULT_PERSON, ADDRESS_STATUS_OTHER)
    val ADDRESS_DELETED = generateAddress(
        PersonGenerator.DEFAULT_PERSON,
        ADDRESS_STATUS_OTHER,
        buildingNumber = "1",
        streetName = "Deleted Close",
        district = "Invisible",
        softDeleted = true
    )

    fun generateStatus(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = AddressStatus(code, description, id)

    fun generateAddress(
        person: Person,
        status: AddressStatus,
        buildingName: String? = null,
        buildingNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        person,
        status,
        buildingName,
        buildingNumber,
        streetName,
        district,
        town,
        county,
        postcode,
        startDate,
        endDate,
        softDeleted,
        id
    )
}
