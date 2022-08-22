package uk.gov.justice.digital.hmpps.integrations.delius.event.manager

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface OrderManagerRepository : JpaRepository<OrderManager, Long> {
    fun findByEventIdAndActiveIsTrueAndSoftDeletedIsFalse(eventId: Long): OrderManager?
}

fun OrderManagerRepository.getByEventIdAndActiveIsTrueAndSoftDeletedIsFalse(eventId: Long): OrderManager =
    findByEventIdAndActiveIsTrueAndSoftDeletedIsFalse(eventId) ?: throw NotFoundException("OrderManager", "eventId", eventId)
