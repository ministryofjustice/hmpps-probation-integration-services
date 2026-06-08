package uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition

import jakarta.persistence.AssociationOverride
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ManagerBaseEntity
import uk.gov.justice.digital.hmpps.jpa.GeneratedId

@Entity
@Table(name = "lic_condition_manager")
@EntityListeners(AuditingEntityListener::class)
@AssociationOverride(name = "team", joinColumns = [JoinColumn(name = "allocated_team_id")])
@AssociationOverride(name = "staff", joinColumns = [JoinColumn(name = "allocated_staff_id")])
@SequenceGenerator(
    name = "lic_condition_manager_id_seq",
    sequenceName = "lic_condition_manager_id_seq",
    allocationSize = 1
)
class LicenceConditionManager(
    @Column(name = "lic_condition_id")
    var licenceConditionId: Long = 0,

    var transferReasonId: Long = 0,

    @Id
    @Column(name = "lic_condition_manager_id")
    @GeneratedId(generator = "lic_condition_manager_id_seq")
    var id: Long = 0
) : ManagerBaseEntity()