package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        select e from Event e
        where e.person.id = :personId 
        and e.person.softDeleted = false
        and e.disposal.disposalType.sentenceType in ('NC', 'SC')
        and e.disposal.active = true and e.active = true
        and e.disposal.softDeleted = false and e.softDeleted = false
        """
    )
    fun findActiveCustodialEvents(personId: Long): List<Event>
}
