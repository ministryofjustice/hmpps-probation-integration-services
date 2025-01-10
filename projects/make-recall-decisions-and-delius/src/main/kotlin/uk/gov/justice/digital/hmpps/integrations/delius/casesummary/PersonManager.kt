package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider

@Immutable
@Table(name = "offender_manager")
@Entity(name = "CaseSummaryPersonManager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true
)

@Immutable
@Table(name = "staff")
@Entity(name = "CaseSummaryStaff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Column
    val forename: String,

    @Column(name = "forename2")
    val middleName: String? = null,

    @Column
    val surname: String
)

@Immutable
@Table(name = "team")
@Entity(name = "CaseSummaryTeam")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Column
    val description: String,

    @Column
    val telephone: String? = null,

    @Column
    val emailAddress: String? = null,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District
)

@Immutable
@Table(name = "district")
@Entity(name = "CaseSummaryDistrict")
class District(
    @Id
    @Column(name = "district_id")
    val id: Long = 0,

    @Column
    val description: String
)

interface CaseSummaryPersonManagerRepository : JpaRepository<PersonManager, Long> {
    @EntityGraph(attributePaths = ["staff", "team.district", "provider"])
    fun findByPersonId(personId: Long): PersonManager?
}
