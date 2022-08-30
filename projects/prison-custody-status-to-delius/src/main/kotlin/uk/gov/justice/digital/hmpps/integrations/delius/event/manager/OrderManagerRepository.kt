package uk.gov.justice.digital.hmpps.integrations.delius.event.manager

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface OrderManagerRepository : JpaRepository<OrderManager, Long> {
    fun findByEventId(eventId: Long): OrderManager?
}

fun OrderManagerRepository.getByEventId(eventId: Long): OrderManager =
    findByEventId(eventId) ?: throw NotFoundException("OrderManager", "eventId", eventId)
