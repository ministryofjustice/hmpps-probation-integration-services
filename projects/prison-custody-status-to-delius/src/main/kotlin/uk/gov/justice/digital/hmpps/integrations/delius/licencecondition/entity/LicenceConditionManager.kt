package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
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
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) : Manager()
