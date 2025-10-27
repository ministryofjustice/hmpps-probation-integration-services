package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.Supervisor
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long = 0,

    @Column(name = "officer_code", columnDefinition = "char(8)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String?,

    @ManyToOne
    @JoinColumn(name = "staff_grade_id")
    val grade: ReferenceData?,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate?,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team>
)

fun Staff.toSupervisor() = Supervisor(
    name = Name(
        forename = this.forename,
        middleName = this.middleName,
        surname = this.surname
    ),
    code = this.code,
    grade = this.grade?.let {
        CodeDescription(
            code = this.grade.code,
            description = this.grade.description
        )
    }
)