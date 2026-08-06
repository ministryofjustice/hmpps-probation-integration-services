package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity

interface EventRepository : JpaRepository<EventEntity, Long> {
    fun findByPersonCrn(crn: String): List<EventEntity>
    fun existsByPersonCrn(crn: String): Boolean
}