package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Disability
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonDetail
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

object CaseDetailsGenerator {
    val MINIMAL_PERSON = generatePerson("M123456", "Minimal", "Person", LocalDate.now().minusYears(27))
    val FULL_PERSON = generatePerson(
        "F987462",
        "Full",
        "Person",
        LocalDate.now().minusYears(42),
        "0191 234 6718",
        "07453351625",
        "someone@somewhere.com",
        ReferenceDataGenerator.GENDER,
        ReferenceDataGenerator.ETHNICITY,
        ReferenceDataGenerator.LANGUAGE,
        ReferenceDataGenerator.RELIGION
    )

    fun generatePerson(
        crn: String,
        forename: String,
        surname: String,
        dob: LocalDate,
        telephone: String? = null,
        mobile: String? = null,
        email: String? = null,
        gender: ReferenceData? = null,
        ethnicity: ReferenceData? = null,
        language: ReferenceData? = null,
        religion: ReferenceData? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonDetail(
        crn,
        forename,
        surname,
        dob,
        telephone,
        mobile,
        email,
        gender,
        ethnicity,
        language,
        religion,
        listOf(),
        softDeleted,
        id
    )

    fun generateAddress(
        status: ReferenceData,
        personDetail: PersonDetail = FULL_PERSON,
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        district: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null,
        noFixedAbode: Boolean = false,
        startDate: LocalDate = LocalDate.now().minusDays(7),
        endDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonAddress(
        personDetail.id,
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

    fun generateDisability(
        type: ReferenceData,
        person: PersonDetail = FULL_PERSON,
        notes: String? = null,
        startDate: LocalDate = LocalDate.now().minusDays(14),
        endDate: LocalDate? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disability(person, type, notes, startDate, endDate, softDeleted, id)
}
