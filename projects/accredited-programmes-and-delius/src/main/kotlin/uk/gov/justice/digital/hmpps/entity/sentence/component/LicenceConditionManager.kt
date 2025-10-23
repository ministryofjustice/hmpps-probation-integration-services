package uk.gov.justice.digital.hmpps.entity.sentence.component

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team

@Entity
@Immutable
@Table(name = "lic_condition_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class LicenceConditionManager(
    @Id
    @Column(name = "lic_condition_manager_id", nullable = false)
    val id: Long,

    @Column(name = "lic_condition_id")
    val licenceConditionId: Long,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)