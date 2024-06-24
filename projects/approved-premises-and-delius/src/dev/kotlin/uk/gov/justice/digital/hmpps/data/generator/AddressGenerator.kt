package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.Address
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object AddressGenerator {

    var PERSON_ADDRESS = generatePersonAddress(
        addressNumber = "12",
        streetName = "Tulip Drive",
        town = "Some Place",
        postcode = "MB01 3TD"
    )

    var INACTIVE_PERSON_ADDRESS = generatePersonAddress(
        personId = PersonGenerator.PERSON_INACTIVE_EVENT.id,
        addressNumber = "12",
        streetName = "Tulip Drive",
        town = "Some Place",
        postcode = "MB01 3TD"
    )

    val Q001 = generateAddress("", "1", "Promise Street", "", "Make Believe", "", "MB01 1PS", "01234567890")
    val Q002 = generateAddress("", "2", "Future Street", "", "Make Believe", "", "MB02 2PS", "01234567891")
    val Q710 = generateAddress("Test AP 10", "10", "Hope Street", "", "Make Believe", "", "MB03 3PS", "01234567892")

    fun generateAddress(
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Address(id, buildingName, addressNumber, streetName, district, town, county, postcode, telephoneNumber)

    fun generatePersonAddress(
        personId: Long = PersonGenerator.DEFAULT.id,
        type: ReferenceData = ReferenceDataGenerator.OWNER_ADDRESS_TYPE,
        status: ReferenceData = ReferenceDataGenerator.MAIN_ADDRESS_STATUS,
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        telephoneNumber: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        id,
        personId,
        type,
        status,
        buildingName,
        addressNumber,
        streetName,
        district,
        town,
        county,
        postcode,
        telephoneNumber
    )
}
