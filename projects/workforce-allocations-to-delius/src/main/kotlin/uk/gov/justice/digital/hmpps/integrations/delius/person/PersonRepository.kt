package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?

    @Query(
        """
        SELECT COUNT(pt) FROM PersonTransfer pt
        JOIN ReferenceData status ON pt.statusId = status.id 
        WHERE pt.personId = :personId 
        AND status.code = 'PN'
        AND pt.softDeleted = false
    """
    )
    fun countPendingTransfers(personId: Long): Int

    @Modifying
    @Query(
        """
    merge into iaps_offender using dual on (offender_id = ?1) 
    when matched then update set iaps_flag=?2 
    when not matched then insert(offender_id, iaps_flag) values(?1,?2)
    """,
        nativeQuery = true
    )
    fun updateIaps(personId: Long, iapsFlagValue: Long = 1)
}
