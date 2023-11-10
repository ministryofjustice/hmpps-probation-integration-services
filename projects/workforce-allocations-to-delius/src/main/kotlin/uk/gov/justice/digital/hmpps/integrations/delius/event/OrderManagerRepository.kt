package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

interface OrderManagerRepository : JpaRepository<OrderManager, Long> {
    @Query(
        """
    select om from OrderManager om 
    where om.eventId = :eventId 
    and om.startDate <= :dateTime 
    and (om.endDate is null or om.endDate > :dateTime)  
    and om.softDeleted = false
    """
    )
    fun findActiveManagerAtDate(eventId: Long, dateTime: ZonedDateTime): OrderManager?
}

fun OrderManagerRepository.getOrderManager(eventId: Long) = findActiveManagerAtDate(eventId, ZonedDateTime.now()) ?: throw NotFoundException("OrderManager", "eventId", eventId)
