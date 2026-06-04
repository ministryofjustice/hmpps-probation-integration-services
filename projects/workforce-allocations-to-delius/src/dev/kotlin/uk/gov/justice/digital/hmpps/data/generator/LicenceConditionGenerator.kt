package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.LicenceCondition
import java.time.LocalDate

object LicenceConditionGenerator {
    val MAIN_CATEGORY = ReferenceData(IdGenerator.getAndIncrement(), "LC01", "Licence Condition Main Category", DatasetGenerator.LIC_COND_TYPE_MAIN_CAT)
    val SUB_CATEGORY = ReferenceData(IdGenerator.getAndIncrement(), "LS01", "Licence Condition Sub Category", DatasetGenerator.LIC_COND_TYPE_SUB_CAT)

    fun generate(
        disposalId: Long,
        mainCategory: ReferenceData = MAIN_CATEGORY,
        subCategory: ReferenceData? = SUB_CATEGORY,
        startDate: LocalDate = LocalDate.now().minusDays(30),
        commenceDate: LocalDate? = LocalDate.now().minusDays(28),
        terminationDate: LocalDate? = null,
        active: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = LicenceCondition(
        id = id,
        disposalId = disposalId,
        mainCategory = mainCategory,
        subCategory = subCategory,
        startDate = startDate,
        commenceDate = commenceDate,
        terminationDate = terminationDate,
        activeFlag = active
    )
}
