package uk.gov.justice.digital.hmpps.integrations.delius.person

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
@AssociationOverride(name = "team", joinColumns = [JoinColumn(name = "team_id")])
@EntityListeners(AuditingEntityListener::class)
@Table(name = "offender_manager")
@SequenceGenerator(name = "offender_manager_id_seq", sequenceName = "offender_manager_id_seq", allocationSize = 1)
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offender_manager_id_seq")
    var id: Long? = null,

    @Column(name = "offender_id")
    val personId: Long
) : ManagerBaseEntity()