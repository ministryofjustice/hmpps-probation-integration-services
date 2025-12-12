package uk.gov.justice.digital.hmpps.integrations.delius.user.staff.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.Caseload
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.entity.Team

@Entity
@Immutable
class Staff(
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Column
    val forename: String,

    @Column
    val surname: String,

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: Provider,

    @OneToMany(mappedBy = "staff")
    val caseLoad: List<Caseload> = emptyList(),

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team>,

    @OneToOne(mappedBy = "staff")
    val user: User? = null,

    @Id
    @Column(name = "staff_id")
    val id: Long
) {
    fun name() = Name(forename, null, surname)
}