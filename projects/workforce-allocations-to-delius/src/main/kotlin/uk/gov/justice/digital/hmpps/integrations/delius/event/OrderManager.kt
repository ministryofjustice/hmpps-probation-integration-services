package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerBaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "order_manager")
@SequenceGenerator(name = "order_manager_id_seq", sequenceName = "order_manager_id_seq", allocationSize = 1)
class OrderManager(
    @Id
    @Column(name = "order_manager_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_manager_id_seq")
    var id: Long = 0,

    var eventId: Long = 0,

    var transferReasonId: Long = 0
) : ManagerBaseEntity()
