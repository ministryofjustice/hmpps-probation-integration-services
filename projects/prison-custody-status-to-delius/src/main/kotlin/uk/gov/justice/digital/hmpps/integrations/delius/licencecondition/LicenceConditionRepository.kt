package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.category.LicenceConditionCategoryCode

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    fun findAllByDisposalIdAndMainCategoryCodeNotAndTerminationReasonIsNull(
        disposalId: Long,
        excludedCategory: String = LicenceConditionCategoryCode.ACCREDITED_PROGRAM.code
    ): List<LicenceCondition>
}
