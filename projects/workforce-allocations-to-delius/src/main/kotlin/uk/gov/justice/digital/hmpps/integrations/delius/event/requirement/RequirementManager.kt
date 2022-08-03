package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ManagerBaseEntity
import javax.persistence.AssociationOverride
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.SequenceGenerator
import javax.persistence.Table

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

    var transferReasonId: Long = 0
) : ManagerBaseEntity()
