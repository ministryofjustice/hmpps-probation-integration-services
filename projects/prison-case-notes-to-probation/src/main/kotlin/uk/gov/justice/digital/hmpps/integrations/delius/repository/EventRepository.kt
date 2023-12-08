package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event

interface EventRepository : JpaRepository<Event, Long> {
    @Query(
        """
        SELECT e.id FROM Event e
        WHERE e.offenderId = :offenderId 
        AND e.disposal.disposalType.sentenceType IN ('NC', 'SC')
        AND e.disposal.active = true AND e.active = true
        AND e.disposal.softDeleted = false AND e.softDeleted = false
    """,
    )
    fun findActiveCustodialEvents(offenderId: Long): List<Long>
}
