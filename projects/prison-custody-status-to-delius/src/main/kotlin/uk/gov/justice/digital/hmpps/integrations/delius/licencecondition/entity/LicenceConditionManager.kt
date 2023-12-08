package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.manager.Manager

@Entity
@Immutable
@Table(name = "lic_condition_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
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
