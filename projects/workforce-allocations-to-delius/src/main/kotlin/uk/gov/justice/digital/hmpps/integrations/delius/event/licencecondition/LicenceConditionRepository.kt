package uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.LicenceConditionTransfer

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {

    @Query(
        """
        SELECT COUNT(lct) FROM LicenceConditionTransfer lct
        JOIN ReferenceData status ON lct.statusId = status.id 
        WHERE lct.licenceConditionId = :licenceConditionId 
        AND status.code = 'PN'
        AND lct.softDeleted = false
    """
    )
    fun countPendingTransfers(licenceConditionId: Long): Int
}

interface LicenceConditionTransferRepository : JpaRepository<LicenceConditionTransfer, Long>
