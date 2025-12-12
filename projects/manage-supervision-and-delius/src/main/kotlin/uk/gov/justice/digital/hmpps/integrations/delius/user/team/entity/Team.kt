package uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity.Staff
import java.time.LocalDate

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Column(name = "description")
    val description: String,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "staff_id")]
    )
    val staff: List<Staff>,

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: Provider,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate? = null
)