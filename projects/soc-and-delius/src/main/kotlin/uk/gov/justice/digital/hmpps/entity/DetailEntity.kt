package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Immutable
@Table(name = "offender")
@Entity
@Where(clause = "soft_deleted = 0")
class DetailPerson(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "char(7)")
    val nomsNumber: String? = null,

    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData?,

    @OneToMany(mappedBy = "person")
    val personManager: List<PersonManager>,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false

)

@Immutable
@Entity
@Where(clause = "active_flag = 1")
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: DetailPerson,

    @Column(name = "probation_area_id")
    val providerId: Long,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: DetailStaff,

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true
)

@Immutable
@Entity
@Table(name = "staff")
class DetailStaff(

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String? = null,

    @Id
    @Column(name = "staff_id")
    val id: Long
)

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: DetailProbationArea,

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    val district: DetailDistrict

)

@Immutable
@Table(name = "probation_area")
@Entity
class DetailProbationArea(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    val description: String,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "district")
class DetailDistrict(

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "district_id")
    val id: Long
)

interface DetailRepository : JpaRepository<DetailPerson, Long> {
    @EntityGraph(
        attributePaths = [
            "religion",
            "personManager",
            "personManager.staff",
            "personManager.team",
            "personManager.team.probationArea",
            "personManager.team.district"
        ]
    )
    fun getByCrn(crn: String): DetailPerson?

    @EntityGraph(
        attributePaths = [
            "religion",
            "personManager",
            "personManager.staff",
            "personManager.team",
            "personManager.team.probationArea",
            "personManager.team.district"
        ]
    )
    fun getByNomsNumber(nomsNumber: String): DetailPerson?

    @Query(
        """
        SELECT json_object(
           'crn' value o.CRN
        )
        FROM OFFENDER o 
        WHERE
            o.crn = :crn
        """,
        nativeQuery = true
    )
    fun getProbationRecord(crn: String): String
}

fun DetailRepository.findByNomsNumber(nomsNumber: String): DetailPerson =
    getByNomsNumber(nomsNumber) ?: throw NotFoundException("person", "nomsNumber", nomsNumber)

fun DetailRepository.findByCrn(crn: String): DetailPerson =
    getByCrn(crn) ?: throw NotFoundException("person", "crn", crn)
