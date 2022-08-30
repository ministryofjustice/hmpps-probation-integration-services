package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.manager

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.manager.Manager
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "lic_condition_manager")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class LicenceConditionManager(
    @Id
    @Column(name = "lic_condition_manager_id", nullable = false)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "lic_condition_id", nullable = false)
    val licenceCondition: LicenceCondition,

    @Column(nullable = false)
    override val staffId: Long,

    @Column(nullable = false)
    override val teamId: Long,

    @Column(nullable = false)
    override val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
) : Manager()
