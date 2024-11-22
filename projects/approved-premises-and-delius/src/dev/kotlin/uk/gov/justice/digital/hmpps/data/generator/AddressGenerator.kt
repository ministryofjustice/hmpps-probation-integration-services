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

    val Q001 = generateAddress(addressNumber = "1", streetName = "Promise Street", postcode = "MB01 1PS", telephoneNumber = "01234567890")
    val Q002 = generateAddress(addressNumber = "2", streetName = "Future Street", postcode = "MB02 2PS", telephoneNumber = "01234567891")
    val Q005 = generateAddress(addressNumber =  "5", streetName = "New Street", postcode = "MB03 3PS", telephoneNumber = "01234567005")
    val Q049 = generateAddress(addressNumber =  "49", streetName = "New Street", postcode = "MB03 3PS", telephoneNumber = "01234567049")
    val Q095 = generateAddress(addressNumber =  "9", streetName = "New Street", postcode = "MB03 3PS", telephoneNumber = "01234567095")
    val Q701 = generateAddress(addressNumber =  "1", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567701")
    val Q702 = generateAddress(addressNumber =  "2", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567702")
    val Q703 = generateAddress(addressNumber =  "3", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567703")
    val Q704 = generateAddress(addressNumber =  "4", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567704")
    val Q705 = generateAddress(addressNumber =  "5", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567705")
    val Q706 = generateAddress(addressNumber =  "6", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567706")
    val Q707 = generateAddress(addressNumber =  "7", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567707")
    val Q708 = generateAddress(addressNumber =  "8", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567708")
    val Q709 = generateAddress(addressNumber =  "9", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567709")
    val Q710 = generateAddress(addressNumber =  "10", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567710")
    val Q711 = generateAddress(addressNumber =  "11", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567711")
    val Q712 = generateAddress(addressNumber =  "12", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567712")
    val Q713 = generateAddress(addressNumber =  "13", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567713")
    val Q714 = generateAddress(addressNumber =  "14", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567714")
    val Q715 = generateAddress(addressNumber =  "15", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567715")
    val Q716 = generateAddress(addressNumber =  "16", streetName = "Hope Street", postcode = "MB03 3PS", telephoneNumber = "01234567716")

    val ALL_ADDRESSES = listOf(Q001, Q002, Q005, Q049, Q095, Q701, Q702, Q703, Q704, Q705, Q706, Q707, Q708, Q709, Q710, Q711, Q712, Q713, Q714, Q715, Q716)

    private fun generateAddress(
        buildingName: String? = "",
        addressNumber: String? = "",
        streetName: String? = null,
        district: String? = "",
        town: String? = "Make Believe",
        county: String? = "",
        postcode: String? = null,
        telephoneNumber: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Address(id, buildingName, addressNumber, streetName, district, town, county, postcode, telephoneNumber)

    private fun generatePersonAddress(
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
