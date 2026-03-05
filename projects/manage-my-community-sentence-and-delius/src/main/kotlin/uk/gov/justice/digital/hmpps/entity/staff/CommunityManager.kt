package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.Person

@Entity
@Immutable
@Table(name = "offender_manager")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class CommunityManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "staff_employee_id")
    val staff: Staff,
    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,
    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person? = null,
    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,
    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)