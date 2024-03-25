package uk.gov.justice.digital.hmpps.controller.casedetails.entity

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface EventRepository : JpaRepository<Event, Long> {
    fun findByIdAndActiveIsTrue(eventId: Long): Event?
}

fun EventRepository.getEvent(eventId: Long): Event =
    findByIdAndActiveIsTrue(eventId) ?: throw NotFoundException("Event", "id", eventId)
