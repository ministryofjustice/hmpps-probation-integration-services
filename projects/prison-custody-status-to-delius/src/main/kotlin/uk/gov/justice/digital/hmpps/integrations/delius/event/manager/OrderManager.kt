package uk.gov.justice.digital.hmpps.integrations.delius.event.manager

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "order_manager")
class OrderManager(
    @Id
    @Column(name = "order_manager_id", nullable = false)
    val id: Long,

    @Column(nullable = false)
    val eventId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "allocation_team_id")
    val teamId: Long,

    @Column
    val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
)
