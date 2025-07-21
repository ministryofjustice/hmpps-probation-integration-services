package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_ADDRESS_STATUS
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_MALE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_NATIONALITY
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.RD_RELIGION
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integration.delius.entity.ReferenceData
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT = generate("A000001", "A0001DY")
    val EXCLUSION = generate("E123456", exclusionMessage = "There is an exclusion on this person")
    val RESTRICTION = generate("R123456", restrictionMessage = "There is a restriction on this person")
    val RESTRICTION_EXCLUSION = generate(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )
    val DEFAULT_2 = generate("A000002", "A0002DY")

    fun generate(
        crn: String,
        nomsId: String? = null,
        forename: String = "Forename",
        surname: String = "Surname",
        dateOfBirth: LocalDate = LocalDate.of(1997, 1, 1),
        pnc: String? = null,
        cro: String? = null,
        currentExclusion: Boolean = false,
        exclusionMessage: String? = null,
        currentRestriction: Boolean = false,
        restrictionMessage: String? = null,
        gender: ReferenceData = RD_MALE,
        religion: ReferenceData? = RD_RELIGION,
        nationality: ReferenceData? = RD_NATIONALITY,
        ethnicity: ReferenceData? = null,
        sexualOrientation: ReferenceData? = null,
        secondName: String? = null,
        thirdName: String? = null,
        telephoneNumber: String? = null,
        mobileNumber: String? = null,
        emailAddress: String? = null,
        currentDisposal: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Person(
        crn,
        forename,
        surname,
        secondName,
        thirdName,
        dateOfBirth,
        nomsId,
        pnc,
        cro,
        currentExclusion,
        exclusionMessage,
        currentRestriction,
        restrictionMessage,
        gender,
        religion,
        nationality,
        ethnicity,
        sexualOrientation,
        telephoneNumber,
        mobileNumber,
        emailAddress,
        listOf(),
        listOf(),
        listOf(),
        currentDisposal,
        softDeleted,
        id
    )

    fun generateAddress(
        person: Person,
        status: ReferenceData = RD_ADDRESS_STATUS,
        buildingName: String? = "buildingName",
        addressNumber: String? = "addressNumber",
        streetName: String? = "streetName",
        district: String? = "district",
        town: String? = "town",
        county: String? = "county",
        postcode: String? = "postcode",
        noFixedAbode: Boolean? = false,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate? = null,
        notes: String? = "Some notes about the address",
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = PersonAddress(
        person,
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
        notes,
        softDeleted,
        id
    )
}