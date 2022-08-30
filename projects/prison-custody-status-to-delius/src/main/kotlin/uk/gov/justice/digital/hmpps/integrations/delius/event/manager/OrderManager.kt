package uk.gov.justice.digital.hmpps.integrations.delius.event.manager

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.manager.Manager
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
@Immutable
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class OrderManager(
    @Id
    @Column(name = "order_manager_id", nullable = false)
    val id: Long,

    @Column(nullable = false)
    val eventId: Long,

    @Column(name = "allocation_staff_id")
    override val staffId: Long,

    @Column(name = "allocation_team_id")
    override val teamId: Long,

    @Column
    override val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
) : Manager()
