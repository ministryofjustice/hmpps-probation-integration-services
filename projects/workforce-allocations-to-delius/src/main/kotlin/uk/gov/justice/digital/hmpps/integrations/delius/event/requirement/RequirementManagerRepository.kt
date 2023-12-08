package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface RequirementManagerRepository : JpaRepository<RequirementManager, Long> {
    @Query(
        """
    select rm from RequirementManager rm 
    where rm.requirementId = :requirementId 
    and rm.startDate <= :dateTime 
    and (rm.endDate is null or rm.endDate > :dateTime)  
    and rm.softDeleted = false
    """,
    )
    fun findActiveManagerAtDate(
        requirementId: Long,
        dateTime: ZonedDateTime,
    ): RequirementManager?
}
