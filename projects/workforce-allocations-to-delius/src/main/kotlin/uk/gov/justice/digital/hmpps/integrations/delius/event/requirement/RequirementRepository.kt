package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface RequirementRepository : JpaRepository<Requirement, Long> {
    @Query(
        """
        SELECT COUNT(rt) FROM RequirementTransfer rt
        JOIN ReferenceData status ON rt.statusId = status.id 
        WHERE rt.requirementId = :requirementId 
        AND status.code = 'PN'
        AND rt.softDeleted = false
    """,
    )
    fun countPendingTransfers(requirementId: Long): Int

    @Modifying
    @Query(
        """
    merge into iaps_rqmnt using dual on (rqmnt_id = ?1) 
    when matched then update set iaps_flag=?2 
    when not matched then insert(rqmnt_id, iaps_flag) values(?1,?2)
    """,
        nativeQuery = true,
    )
    fun updateIaps(
        requirementId: Long,
        iapsFlagValue: Long = 1,
    )
}
