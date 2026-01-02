package uk.gov.justice.digital.hmpps.integrations.delius.event

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ManagerBaseEntity
import uk.gov.justice.digital.hmpps.jpa.GeneratedId

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "order_manager")
@SequenceGenerator(name = "order_manager_id_seq", sequenceName = "order_manager_id_seq", allocationSize = 1)
class OrderManager(
    var eventId: Long = 0,

    var transferReasonId: Long = 0,

    @Id
    @Column(name = "order_manager_id")
    @GeneratedId(generator = "order_manager_id_seq")
    var id: Long = 0
) : ManagerBaseEntity()
