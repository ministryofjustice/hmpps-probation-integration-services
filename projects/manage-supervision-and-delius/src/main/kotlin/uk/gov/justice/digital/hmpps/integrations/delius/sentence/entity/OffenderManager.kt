package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import java.io.Serializable
import java.time.LocalDate
import kotlin.jvm.Transient

@Entity
@Immutable
class OffenderManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    val endDate: LocalDate?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
)

@Immutable
@Entity(name = "contact_staff")
@Table(name = "staff")
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long,

    val forename: String,
    val surname: String,

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: Provider,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser?
)


@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Column(name = "distinguished_name")
    val username: String,

    ) {
    @Transient
    var email: String? = null

    @Transient
    var telephone: String? = null
}

interface StaffUserRepository : JpaRepository<StaffUser, Long> {

    @Query(
        """
            SELECT u.id AS userId, st.id AS staffId, t.id AS teamId, st.provider.id AS providerId, l.id AS locationId
            FROM StaffUser u
            JOIN  u.staff st 
            JOIN  st.provider
            JOIN  Team t ON t.provider = st.provider
            JOIN  TeamOfficeLink tol ON tol.id.teamId = t.id
            JOIN  Location l ON l = tol.id.officeLocation
            WHERE UPPER(u.username) = UPPER(:username)
            AND UPPER(t.description) = UPPER(:teamName)
        """
    )
    fun findUserAndLocation(username: String, teamName: String ): UserLocation?
}

fun StaffUserRepository.getUserAndLocation(username: String, teamName: String) =
    findUserAndLocation(username, teamName) ?: throw NotFoundException("User", "username",
        "$username in $teamName"
    )

interface UserLocation {
    val userId: Long
    val staffId: Long
    val teamId: Long
    val providerId: Long
    val locationId: Long
}

@Immutable
@Entity(name = "professional_contact_team")
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    val provider: Provider,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
    val description: String,
)

@Immutable
@Entity
@Table(name = "team_office_location")
class TeamOfficeLink(
    @Id
    val id: TeamOfficeLinkId
)

@Immutable
@Entity
@Table(name = "office_location")
class Location(
    @Id
    @Column(name = "office_location_id")
    val id: Long,

    val code: String,

    val description: String,

)


@Embeddable
class TeamOfficeLinkId(
    @Column(name = "team_id")
    val teamId: Long,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val officeLocation: Location
) : Serializable

