package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        select e from Event e
        where e.person.id = :personId 
        and e.person.softDeleted = false
        and e.disposal.type.sentenceType in ('NC', 'SC')
        and e.disposal.active = true and e.active = true
        and e.disposal.softDeleted = false and e.softDeleted = false
        """
    )
    fun findActiveCustodialEvents(personId: Long): List<Event>

    @Modifying
    @Query(
        """
        merge into iaps_event using dual on (event_id = ?1) 
        when matched then update set iaps_flag = ?2 
        when not matched then insert(event_id, iaps_flag) values(?1, ?2)
        """,
        nativeQuery = true
    )
    fun updateIaps(eventId: Long, iapsFlagValue: Long = 1)
}
