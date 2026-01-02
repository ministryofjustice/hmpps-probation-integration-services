package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ManagerBaseEntity
import uk.gov.justice.digital.hmpps.jpa.GeneratedId

@Entity
@Table(name = "rqmnt_manager")
@EntityListeners(AuditingEntityListener::class)
@AssociationOverride(name = "team", joinColumns = [JoinColumn(name = "allocated_team_id")])
@AssociationOverride(name = "staff", joinColumns = [JoinColumn(name = "allocated_staff_id")])
@SequenceGenerator(name = "rqmnt_manager_id_seq", sequenceName = "rqmnt_manager_id_seq", allocationSize = 1)
class RequirementManager(
    @Column(name = "rqmnt_id")
    var requirementId: Long = 0,

    var transferReasonId: Long = 0,

    @Id
    @Column(name = "rqmnt_manager_id")
    @GeneratedId(generator = "rqmnt_manager_id_seq")
    var id: Long = 0
) : ManagerBaseEntity()
