package uk.gov.justice.digital.hmpps.integrations.delius.user

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithTeams
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser
import java.io.Serializable

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @Id
    @Column(name = "user_id")
    val id: Long = 0,

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: StaffWithUser? = null
)

@Entity
@Immutable
@Table(name = "user_")
class StaffWithTeamUser(
    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: StaffWithTeams? = null,

    @JoinTable(
        name = "probation_area_user",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "probation_area_id")]
    )
    @ManyToMany
    val datasets: List<Provider>,

    @Id
    @Column(name = "user_id")
    val id: Long = 0,
)

@Entity
@Immutable
@Table(name = "probation_area_user")
class ProbationAreaUser(
    @EmbeddedId
    val id: ProbationAreaUserId,
)

@Embeddable
class ProbationAreaUserId(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: StaffWithTeamUser? = null,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider
) : Serializable
