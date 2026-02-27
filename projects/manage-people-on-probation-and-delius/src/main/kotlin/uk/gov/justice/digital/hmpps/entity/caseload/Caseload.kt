package uk.gov.justice.digital.hmpps.entity.caseload

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.user.Staff
import uk.gov.justice.digital.hmpps.entity.user.Team

@Entity
@Immutable
@SQLRestriction("role_code = 'OM'")
data class Caseload(
    @Id
    @Column(name = "caseload_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "staff_employee_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "trust_provider_team_id")
    val team: Team,

    @Column(name = "role_code")
    val roleCode: String,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,
)