package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseEntity
import java.time.LocalDate

object CaseGenerator {
    val DEFAULT = generate("D001022")

    val EXCLUSION = generate("E123456", exclusionMessage = "There is an exclusion on this person")
    val RESTRICTION = generate("R123456", restrictionMessage = "There is a restriction on this person")
    val RESTRICTION_EXCLUSION = generate(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    fun generate(
        crn: String, id: Long = IdGenerator.getAndIncrement(),
        exclusionMessage: String? = null,
        restrictionMessage: String? = null
    ) =
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
            provisions = listOf(),
            ethnicity = ReferenceDataGenerator.ETHNICITY_INDIAN,
            primaryLanguage = ReferenceDataGenerator.LANGUAGE_ENGLISH,
            registrations = listOf(),
            exclusionMessage = exclusionMessage,
            restrictionMessage = restrictionMessage
        )
}
