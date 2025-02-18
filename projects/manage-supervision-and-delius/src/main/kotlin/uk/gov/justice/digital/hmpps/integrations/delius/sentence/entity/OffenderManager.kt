package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import java.io.Serializable
import java.time.LocalDate
import java.time.ZonedDateTime
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

    val allocationDate: LocalDate,

    val endDate: LocalDate? = null,

    @OneToOne
    @JoinColumns(
        JoinColumn(
            name = "offender_manager_id",
            referencedColumnName = "offender_manager_id",
            insertable = false,
            updatable = false,
        ), JoinColumn(name = "offender_id", referencedColumnName = "offender_id", insertable = false, updatable = false)
    )
    val responsibleOfficer: ResponsibleOfficer? = null,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
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

    val forename: String,

    val forename2: String? = null,

    val surname: String
) {
    @Transient
    var email: String? = null

    @Transient
    var telephone: String? = null
}

@Entity
@Table(name = "responsible_officer")
@SQLRestriction("end_date IS NULL")
class ResponsibleOfficer(
    @Id
    @Column(name = "responsible_officer_id", nullable = false)
    val id: Long = 0,

    @Column(name = "offender_id")
    val personId: Long,

    @OneToOne
    @JoinColumn(name = "prison_offender_manager_id")
    val prisonManager: PrisonManager? = null,

    val startDate: ZonedDateTime,

    val endDate: ZonedDateTime? = null,

    @Column(name = "offender_manager_id")
    val offenderManagerId: Long? = null
)

interface StaffUserRepository : JpaRepository<StaffUser, Long> {

    @Query(
        """
            SELECT u 
            FROM StaffUser u
            WHERE UPPER(u.username) = UPPER(:username)
        """
    )
    fun findByUsername(username: String): StaffUser?

    @Query(
        """
            SELECT u.id AS userId, st.id AS staffId, t.id AS teamId, st.provider.id AS providerId, l.id AS locationId
            FROM StaffUser u
            JOIN  u.staff st
            JOIN  ContactStaffTeam cst ON cst.id.staffId = st.id
            JOIN  Team t ON t.id = cst.id.team.id
            JOIN  TeamOfficeLink tol ON tol.id.teamId = t.id
            JOIN  Location l ON l = tol.id.officeLocation
            WHERE UPPER(u.username) = UPPER(:username)
            AND l.id = :locationId
        """
    )
    fun findUserAndLocation(username: String, locationId: Long): UserLocation?

    @Query(
        """
            SELECT l
            FROM StaffUser u
            JOIN u.staff st
            JOIN ContactStaffTeam cst ON cst.id.staffId = st.id
            JOIN Team t ON t.id = cst.id.team.id
            JOIN TeamOfficeLink tol ON tol.id.teamId = t.id
            JOIN Location l ON l = tol.id.officeLocation
            WHERE u.id = :id
        """
    )
    fun findUserOfficeLocations(id: Long): List<Location>
}

fun StaffUserRepository.getUser(username: String) =
    findByUsername(username) ?: throw NotFoundException("User", "username", username)

fun StaffUserRepository.getUserAndLocation(username: String, locationId: Long) =
    findUserAndLocation(username, locationId) ?: throw NotFoundException(
        "User", "username",
        "$username in $locationId"
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

    val buildingName: String?,

    val buildingNumber: String?,

    val streetName: String?,

    val townCity: String?,

    val county: String?,

    val postcode: String?,
)

@Embeddable
class TeamOfficeLinkId(
    @Column(name = "team_id")
    val teamId: Long,

    @ManyToOne
    @JoinColumn(name = "office_location_id")
    val officeLocation: Location
) : Serializable

@Entity
@Immutable
@Table(name = "staff_team")
class ContactStaffTeam(
    @Id
    val id: StaffTeamLinkId
)

@Embeddable
class StaffTeamLinkId(
    @Column(name = "staff_id")
    val staffId: Long,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team
) : Serializable
