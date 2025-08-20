package uk.gov.justice.digital.hmpps.entity.sentence.component

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team

@Entity
@Immutable
@Table(name = "rqmnt_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class RequirementManager(
    @Id
    @Column(name = "rqmnt_manager_id", nullable = false)
    val id: Long,

    @Column(name = "rqmnt_id")
    val requirementId: Long,

    @ManyToOne
    @JoinColumn(name = "allocated_staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "allocated_team_id")
    val team: Team,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)