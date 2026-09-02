package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.PersonDetail
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
    val REGISTERED_PERSON =
        generate("R123456", firstName = "Reginald", surname = "Regency", dob = LocalDate.now().minusYears(27))
    val STANDALONE_ONLY_PERSON =
        generate("S123456", firstName = "Standalone", surname = "Standalone", dob = LocalDate.now().minusYears(31))
    val UPW_ONLY_PERSON =
        generate("U123456", firstName = "Upw", surname = "Upw", dob = LocalDate.now().minusYears(30))
    val NO_EVENTS_PERSON =
        generate("P123456", firstName = "NoEvents", surname = "NoEvents", dob = LocalDate.now().minusYears(28))
    val MIXED_ORDERS_PERSON =
        generate("M123456", firstName = "Mixed", surname = "Mixed", dob = LocalDate.now().minusYears(34))
    val NO_DISPOSAL_PERSON =
        generate("N123456", firstName = "NoDisposal", surname = "NoDisposal", dob = LocalDate.now().minusYears(29))
    val EMPTY_REQUIREMENTS_PERSON =
        generate("E123456", firstName = "Empty", surname = "Empty", dob = LocalDate.now().minusYears(33))

    val GENDER = ReferenceDataGenerator.generate("GEN", "Gender")
    val ETHNICITY = ReferenceDataGenerator.generate("ETH", "Ethnicity")
    val LANGUAGE = ReferenceDataGenerator.generate("LAN", "Language")
    val RELIGION = ReferenceDataGenerator.generate("REL", "Religion")

    val MAIN_ADDRESS_STATUS = ReferenceDataGenerator.generate(PersonAddress.MAIN_STATUS_CODE, "Main Address")

    val DETAILED_PERSON =
        generate(
            crn = "D123456",
            firstName = "Daniel",
            secondName = "David",
            surname = "Danube",
            dob = LocalDate.now().minusYears(36),
            noms = "D1234YZ",
            pnc = "2011/0593710D",
            cro = "89861/11W",
            gender = GENDER,
            ethnicity = ETHNICITY,
            language = LANGUAGE,
            religion = RELIGION,
            emailAddress = "dan@gmail.com",
            telephoneNumber = "0191 256 7234",
            mobileNumber = "07345617263"
        )

    val DETAIL_ADDRESS = generateAddress(
        DETAILED_PERSON.id,
        MAIN_ADDRESS_STATUS,
        addressNumber = "23",
        streetName = "Mantle Place",
        postcode = "H34 7TH"
    )

    fun generate(
        crn: String,
        noms: String? = null,
        pnc: String? = null,
        cro: String? = null,
        firstName: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
        dob: LocalDate,
        telephoneNumber: String? = null,
        mobileNumber: String? = null,
        emailAddress: String? = null,
        gender: ReferenceData? = null,
        ethnicity: ReferenceData? = null,
        language: ReferenceData? = null,
        religion: ReferenceData? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonDetail(
        crn,
        noms,
        pnc,
        cro,
        firstName,
        secondName,
        thirdName,
        surname,
        dob,
        telephoneNumber,
        mobileNumber,
        emailAddress,
        gender,
        ethnicity,
        language,
        religion,
        softDeleted,
        id
    )

    fun generatePerson(
        crn: String,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, softDeleted, id)

    fun generateAddress(
        personId: Long,
        status: ReferenceData,
        noFixedAbode: Boolean = false,
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        startDate: LocalDate = LocalDate.now().minusDays(1),
        endDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        personId,
        status,
        buildingName,
        addressNumber,
        streetName,
        district,
        town,
        county,
        postcode,
        noFixedAbode,
        startDate,
        endDate,
        softDeleted,
        id
    )
}

fun PersonDetail.asPerson() = Person(crn, softDeleted, id)