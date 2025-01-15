package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.manager.Manager

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class OrderManager(
    @Id
    @Column(name = "order_manager_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "allocation_staff_id")
    override val staffId: Long,

    @Column(name = "allocation_team_id")
    override val teamId: Long,

    @Column
    override val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) : Manager()

interface OrderManagerRepository : JpaRepository<OrderManager, Long> {
    fun findByEventId(eventId: Long): OrderManager?
}

fun OrderManagerRepository.getByEventId(eventId: Long): OrderManager =
    findByEventId(eventId) ?: throw NotFoundException("OrderManager", "eventId", eventId)
