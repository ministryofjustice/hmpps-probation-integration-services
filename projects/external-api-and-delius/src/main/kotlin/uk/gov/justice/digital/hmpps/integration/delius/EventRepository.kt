package uk.gov.justice.digital.hmpps.integration.delius

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integration.delius.entity.Event

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonIdOrderByConvictionDateDesc(personId: Long): List<Event>
}
