package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity

interface EventRepository : JpaRepository<EventEntity, Long> {

    @Query(
        """
        select e from EventEntity e 
        where e.person.crn = :crn
    """
    )
    fun findByCrn(crn: String): List<EventEntity>
}