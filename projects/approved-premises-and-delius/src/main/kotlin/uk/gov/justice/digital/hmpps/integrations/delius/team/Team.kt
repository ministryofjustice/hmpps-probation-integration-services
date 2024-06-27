package uk.gov.justice.digital.hmpps.integrations.delius.team

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.person.Ldu
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import java.time.LocalDate

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,

    @OneToOne
    @JoinTable(
        name = "r_approved_premises_team",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "approved_premises_id")]
    )
    val approvedPremises: ApprovedPremises?,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: Ldu,

    @Column(name = "start_date")
    val startDate: LocalDate = LocalDate.now(),

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    )
