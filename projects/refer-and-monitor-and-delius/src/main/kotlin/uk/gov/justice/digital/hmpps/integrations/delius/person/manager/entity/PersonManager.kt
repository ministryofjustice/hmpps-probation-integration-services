package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @OneToOne(mappedBy = "communityManager")
    val responsibleOfficer: ResponsibleOfficer?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "prison_offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PrisonManager(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "allocation_team_id")
    val team: Team,

    @OneToOne(mappedBy = "prisonManager")
    val responsibleOfficer: ResponsibleOfficer?,

    val emailAddress: String?,

    val telephoneNumber: String?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "prison_offender_manager_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "responsible_officer")
@SQLRestriction("end_date is null")
class ResponsibleOfficer(
    @OneToOne
    @JoinColumn(name = "offender_manager_id")
    val communityManager: PersonManager?,

    @OneToOne
    @JoinColumn(name = "prison_offender_manager_id")
    val prisonManager: PrisonManager? = null,

    val endDate: ZonedDateTime? = null,

    @Id
    @Column(name = "responsible_officer_id")
    val id: Long
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @EntityGraph(attributePaths = ["person", "responsibleOfficer", "staff.user", "team.district.borough"])
    fun findByPersonCrn(crn: String): PersonManager?

    @Query(
        """
        select p.crn 
        from PersonManager pm
        join pm.person p
        join pm.staff.user u
        where upper(u.username) = upper(:username)
    """
    )
    fun findCasesManagedBy(username: String): List<String>
}

interface PrisonManagerRepository : JpaRepository<PrisonManager, Long> {
    @EntityGraph(attributePaths = ["responsibleOfficer", "staff", "team.district.borough"])
    fun findByPersonId(personId: Long): PrisonManager?
}
