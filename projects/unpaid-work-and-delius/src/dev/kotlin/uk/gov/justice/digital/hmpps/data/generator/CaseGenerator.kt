package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseEntity
import java.time.LocalDate

object CaseGenerator {
    val DEFAULT = generate("D001022")

    fun generate(crn: String, id: Long = IdGenerator.getAndIncrement()) =
        CaseEntity(id,
            crn,
            false,
            "David",
            null,
            null,
            "Banner",
            LocalDate.now().minusYears(19),
            ReferenceDataGenerator.GENDER_MALE,
            personalCircumstances =  listOf(),
            personalContacts = listOf(),
            aliases = listOf(),
            disabilities = listOf(),
            ethnicity = ReferenceDataGenerator.ETHNICITY_INDIAN,
            primaryLanguage = ReferenceDataGenerator.LANGUAGE_ENGLISH
        )

}
