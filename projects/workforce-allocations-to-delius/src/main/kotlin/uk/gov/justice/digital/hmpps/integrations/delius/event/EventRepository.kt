package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface EventRepository : JpaRepository<Event, Long> {
    @Modifying
    @Query(
        """
    merge into iaps_event using dual on (event_id = ?2) 
    when matched then update set iaps_flag=?1 
    when not matched then insert(event_id, iaps_flag) values(?2,?1)
    """,
        nativeQuery = true
    )
    fun updateIaps(eventId: Long, iapsFlagValue: Long = 1)
}
