package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ManagerBaseEntity
import uk.gov.justice.digital.hmpps.jpa.GeneratedId

@Entity
@EntityListeners(AuditingEntityListener::class)
@AssociationOverride(name = "team", joinColumns = [JoinColumn(name = "team_id")])
@Table(name = "offender_manager")
@SequenceGenerator(name = "offender_manager_id_seq", sequenceName = "offender_manager_id_seq", allocationSize = 1)
class PersonManager(
    @Column(name = "offender_id")
    var personId: Long = 0,

    @Id
    @Column(name = "offender_manager_id")
    @GeneratedId(generator = "offender_manager_id_seq")
    var id: Long = 0
) : ManagerBaseEntity()
