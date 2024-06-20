package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Immutable
@Table(name = "offender")
@Entity
@SQLRestriction("soft_deleted = 0")
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

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    val nationality: ReferenceData?,

    @OneToMany(mappedBy = "person")
    val personManager: List<PersonManager>,

    @OneToMany(mappedBy = "person")
    val offenderAliases: List<OffenderAlias>,

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

    @Column(name = "current_disposal", columnDefinition = "number")
    val currentDisposal: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false

)

@Immutable
@Entity
@SQLRestriction("active_flag = 1")
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: DetailPerson,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: DetailProbationArea,

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
    @JoinColumn(name = "district_id")
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

@Immutable
@Entity
@Table(name = "alias")
@SQLRestriction("soft_deleted = 0")
class OffenderAlias(

    @Id
    @Column(name = "alias_id")
    val aliasID: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: DetailPerson,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "first_name")
    val firstName: String,

    @Column(name = "second_name")
    val secondName: String? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    val surname: String,

    @Column(name = "third_name")
    val thirdName: String? = null,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData
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
}

fun DetailRepository.findByNomsNumber(nomsNumber: String): DetailPerson =
    getByNomsNumber(nomsNumber) ?: throw NotFoundException("person", "nomsNumber", nomsNumber)

fun DetailRepository.findByCrn(crn: String): DetailPerson =
    getByCrn(crn) ?: throw NotFoundException("person", "crn", crn)
