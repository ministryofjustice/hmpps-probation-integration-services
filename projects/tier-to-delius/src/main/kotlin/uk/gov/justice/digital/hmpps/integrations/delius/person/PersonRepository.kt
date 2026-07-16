package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?
    fun existsByCrnAndSoftDeletedIsFalse(crn: String): Boolean

    @Query(
        """
        select distinct p.crn 
        from EventEntity e
        join e.disposal d
        join e.person p 
        where e.softDeleted = false and e.active = true
        and d.softDeleted = false and d.active = true
        and p.softDeleted = false
        """
    )
    fun findAllCrns(): List<String>

    @Modifying
    @Query("update PersonWithV3Tier p set p.v3TierId = :v3TierId where p.crn = :crn")
    fun updateV3TierColumn(crn: String, v3TierId: Long)
}
