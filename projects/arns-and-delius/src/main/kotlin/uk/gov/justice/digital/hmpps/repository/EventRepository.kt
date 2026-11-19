package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.Event

interface EventRepository: JpaRepository<Event, Long> {
    fun findAllByPerson_CrnAndActiveFlagTrue(crn: String): List<Event>
}