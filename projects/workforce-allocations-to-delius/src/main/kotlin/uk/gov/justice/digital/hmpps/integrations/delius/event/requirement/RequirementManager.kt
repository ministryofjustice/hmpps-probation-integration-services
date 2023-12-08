package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import jakarta.persistence.AssociationOverride
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ManagerBaseEntity

@Entity
@Table(name = "rqmnt_manager")
@EntityListeners(AuditingEntityListener::class)
@AssociationOverride(name = "team", joinColumns = [JoinColumn(name = "allocated_team_id")])
@AssociationOverride(name = "staff", joinColumns = [JoinColumn(name = "allocated_staff_id")])
@SequenceGenerator(name = "rqmnt_manager_id_seq", sequenceName = "rqmnt_manager_id_seq", allocationSize = 1)
class RequirementManager(
    @Id
    @Column(name = "rqmnt_manager_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rqmnt_manager_id_seq")
    var id: Long = 0,
    @Column(name = "rqmnt_id")
    var requirementId: Long = 0,
    var transferReasonId: Long = 0,
) : ManagerBaseEntity()
