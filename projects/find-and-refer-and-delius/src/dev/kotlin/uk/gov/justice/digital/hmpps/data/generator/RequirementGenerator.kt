package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_1
import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object RequirementGenerator {
    val RMC38 = RequirementMainCategory(id = IdGenerator.getAndIncrement(), code = "RM38")
    val RMC_7 = RequirementMainCategory(id = IdGenerator.getAndIncrement(), code = "7")
    val RMC_OTHER = RequirementMainCategory(id = IdGenerator.getAndIncrement(), code = "O")
    val SUB_CAT = ReferenceData(id = IdGenerator.getAndIncrement(), code = "SCAT", description = "Sub Category")
    val TERMINATION_DETAILS =
        ReferenceData(id = IdGenerator.getAndIncrement(), code = "TD", description = "Termination Details")
    val AMC_RMC38 = RequirementAdditionalMainCategory(id = IdGenerator.getAndIncrement(), code = "RM38")
    val AMC_7 = RequirementAdditionalMainCategory(id = IdGenerator.getAndIncrement(), code = "7")

    val ACC_PROG_1 = generateRequirement(
        person = PERSON_1,
        mainCategory = RMC38,
        additionalMainCategory = null,
        subCategory = SUB_CAT,
        endDate = LocalDate.now().minusDays(1),
        terminationReason = TERMINATION_DETAILS,
    )

    val ACC_PROG_2 = generateRequirement(
        person = PERSON_1,
        mainCategory = RMC_7,
        additionalMainCategory = null,
        subCategory = SUB_CAT,
        endDate = LocalDate.now().minusDays(2),
        terminationReason = TERMINATION_DETAILS,
    )

    val ACC_PROG_3 = generateRequirement(
        person = PERSON_1,
        mainCategory = RMC_OTHER,
        additionalMainCategory = AMC_RMC38,
        subCategory = SUB_CAT,
        endDate = LocalDate.now().minusDays(3),
        terminationReason = TERMINATION_DETAILS,
    )

    val ACC_PROG_4 = generateRequirement(
        person = PERSON_1,
        mainCategory = RMC_OTHER,
        additionalMainCategory = AMC_7,
        subCategory = SUB_CAT,
        endDate = LocalDate.now().minusDays(4),
        terminationReason = TERMINATION_DETAILS,
    )

    val ACC_PROG_5 = generateRequirement(
        person = PERSON_1,
        mainCategory = RMC_OTHER,
        additionalMainCategory = AMC_7,
        subCategory = SUB_CAT,
        terminationReason = TERMINATION_DETAILS,
        active = true,
        startDate = LocalDate.now().minusDays(9),
    )

    val ACC_PROG_6 = generateRequirement(
        person = PERSON_1,
        mainCategory = RMC_OTHER,
        additionalMainCategory = AMC_7,
        subCategory = SUB_CAT,
        terminationReason = TERMINATION_DETAILS,
        active = true,
        startDate = LocalDate.now().minusDays(8),
    )

    private fun generateRequirement(
        person: Person,
        mainCategory: RequirementMainCategory,
        additionalMainCategory: RequirementAdditionalMainCategory? = null,
        subCategory: ReferenceData,
        terminationReason: ReferenceData? = null,
        startDate: LocalDate = LocalDate.now().minusDays(10),
        endDate: LocalDate? = null,
        active: Boolean = false,
        notes: String? = "Some notes",
    ) =
        Requirement(
            id = IdGenerator.getAndIncrement(),
            person = person,
            active = active,
            startDate = startDate,
            endDate = endDate,
            terminationDetails = terminationReason,
            mainCategory = mainCategory,
            subCategory = subCategory,
            additionalMainCategory = additionalMainCategory,
            notes = notes,
            softDeleted = false
        )
}
