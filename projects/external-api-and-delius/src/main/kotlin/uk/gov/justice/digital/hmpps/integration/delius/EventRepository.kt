package uk.gov.justice.digital.hmpps.integration.delius

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.entity.Event

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonIdOrderByConvictionDateDesc(personId: Long): List<Event>
    fun findByPersonCrnAndNumber(crn: String, eventNumber: String): Event?
}

fun EventRepository.getEvent(crn: String, number: String) =
    findByPersonCrnAndNumber(crn, number) ?: throw NotFoundException("Event $number not found for $crn")
