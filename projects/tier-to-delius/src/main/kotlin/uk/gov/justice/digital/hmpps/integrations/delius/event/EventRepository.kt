package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.flagged.EventWithSa2026Exclusions

interface EventRepository : JpaRepository<EventEntity, Long> {
    fun findByPersonCrn(crn: String): List<EventEntity>
    fun existsByPersonCrn(crn: String): Boolean
}

interface EventWithSa2026ExclusionsRepository : JpaRepository<EventWithSa2026Exclusions, Long> {
    fun findByPersonCrn(crn: String): List<EventWithSa2026Exclusions>
    fun existsByPersonCrn(crn: String): Boolean
}