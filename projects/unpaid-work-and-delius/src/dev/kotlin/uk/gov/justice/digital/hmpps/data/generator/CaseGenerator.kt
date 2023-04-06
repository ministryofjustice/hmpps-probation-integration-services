package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseEntity
import java.time.LocalDate

object CaseGenerator {
    val DEFAULT = generate("D001022")

    fun generate(crn: String, id: Long = IdGenerator.getAndIncrement()) =
        CaseEntity(
            id,
            crn,
            false,
            "David",
            null,
            null,
            "Banner",
            ReferenceDataGenerator.GENDER_MALE,
            LocalDate.now().minusYears(19),
            emailAddress = "myemail@email.com",
            telephoneNumber = "123123",
            mobileNumber = "123123",
            personalCircumstances = listOf(),
            personalContacts = listOf(),
            aliases = listOf(),
            disabilities = listOf(),
            ethnicity = ReferenceDataGenerator.ETHNICITY_INDIAN,
            primaryLanguage = ReferenceDataGenerator.LANGUAGE_ENGLISH,
            registrations = listOf(),
            addresses = listOf()
        )
}
