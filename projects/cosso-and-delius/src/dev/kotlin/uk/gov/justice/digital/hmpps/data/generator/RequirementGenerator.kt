package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.RequirementEntity
import uk.gov.justice.digital.hmpps.entity.RequirementType
import java.time.LocalDate

object RequirementGenerator {
    val DEFAULT_REQUIREMENT = RequirementEntity(
        id = IdGenerator.getAndIncrement(),
        disposalId = DisposalGenerator.DEFAULT_DISPOSAL.id,
        startDate = LocalDate.now().minusDays(6),
        requirementType = getRequirementType("Probation"),
        requirementSubType = ReferenceDataGenerator.DEFAULT_REQUIREMENT_SUBTYPE,
        length = 2,
        length2 = 1,
        softDeleted = false

    )

    fun getRequirementType(typeDescription: String) = RequirementType(
        id = IdGenerator.getAndIncrement(),
        description = typeDescription,
        units = ReferenceDataGenerator.LENGTH_UNITS_MONTHS,
        length2Units = ReferenceDataGenerator.LENGTH_UNITS_DAYS
    )
}