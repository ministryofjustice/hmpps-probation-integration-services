package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.CommunityManagerEntity

interface CommunityManagerRepository : JpaRepository<CommunityManagerEntity, Long> {
    @Query(
        """
            select cm
            from CommunityManagerEntity cm 
            join fetch cm.staff 
            where cm.person.nomsNumber = :nomsNumber 
            and cm.person.softDeleted = false
            and cm.active = true and cm.softDeleted = false
        """
    )
    fun findCommunityManager(nomsNumber: String): CommunityManagerEntity?
}
