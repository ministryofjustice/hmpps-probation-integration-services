package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class OrderManager(
    @Id
    @Column(name = "order_manager_id", nullable = false)
    val id: Long,

    @Column
    val eventId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "allocation_team_id")
    val teamId: Long,

    @Column(name = "probation_area_id")
    val providerId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false
)

interface OrderManagerRepository : JpaRepository<OrderManager, Long> {
    fun findByEventId(eventId: Long): OrderManager?
}

fun OrderManagerRepository.getByEventId(eventId: Long): OrderManager =
    findByEventId(eventId) ?: throw NotFoundException("OrderManager", "eventId", eventId)
