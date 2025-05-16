package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import uk.gov.justice.digital.hmpps.entity.DetailPerson.Companion.CRN
import uk.gov.justice.digital.hmpps.entity.DetailPerson.Companion.DOB
import uk.gov.justice.digital.hmpps.entity.DetailPerson.Companion.FORENAME
import uk.gov.justice.digital.hmpps.entity.DetailPerson.Companion.NOMS
import uk.gov.justice.digital.hmpps.entity.DetailPerson.Companion.PNC
import uk.gov.justice.digital.hmpps.entity.DetailPerson.Companion.SURNAME
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
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    val nationality: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData?,

    @OneToMany(mappedBy = "person")
    val personManager: List<PersonManager>,

    @OneToMany(mappedBy = "person")
    val offenderAliases: List<PersonAlias>,

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
    @Convert(converter = NumericBooleanConverter::class)
    val currentDisposal: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
) {
    companion object {
        val CRN = DetailPerson::crn.name
        val FORENAME = DetailPerson::forename.name
        val SURNAME = DetailPerson::surname.name
        val DOB = DetailPerson::dateOfBirth.name
        val NOMS = DetailPerson::nomsNumber.name
        val PNC = DetailPerson::pncNumber.name
    }
}

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
    @JoinColumn(name = "allocation_staff_id")
    val staff: DetailStaff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true
)

@Immutable
@Entity
@Table(name = "staff")
class DetailStaff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String? = null,

    @Id
    @Column(name = "staff_id")
    val id: Long
) {
    fun unallocated(): Boolean = code.endsWith("U")
}

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
    val probationArea: DetailProbationArea,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: DetailDistrict

)

@Immutable
@Table(name = "probation_area")
@Entity
class DetailProbationArea(

    @Column
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
class PersonAlias(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: DetailPerson,

    @Column(name = "first_name")
    val firstName: String,

    @Column(name = "second_name")
    val secondName: String? = null,

    @Column(name = "third_name")
    val thirdName: String? = null,

    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "alias_id")
    val aliasID: Long,
)

interface DetailRepository : JpaRepository<DetailPerson, Long>, JpaSpecificationExecutor<DetailPerson> {
    @EntityGraph(
        attributePaths = [
            "nationality",
            "religion",
            "personManager.staff",
            "personManager.team.probationArea",
            "personManager.team.district"
        ]
    )
    fun getByCrn(crn: String): DetailPerson?

    @EntityGraph(
        attributePaths = [
            "nationality",
            "religion",
            "personManager.staff",
            "personManager.team.probationArea",
            "personManager.team.district"
        ]
    )
    fun getByNomsNumber(nomsNumber: String): DetailPerson?

    @EntityGraph(
        attributePaths = [
            "nationality",
            "religion",
            "personManager.staff",
            "personManager.team.probationArea",
            "personManager.team.district"
        ]
    )
    fun findByCrnIn(crns: Set<String>): List<DetailPerson>
}

fun DetailRepository.findByNomsNumber(nomsNumber: String): DetailPerson =
    getByNomsNumber(nomsNumber) ?: throw NotFoundException("person", "nomsNumber", nomsNumber)

fun DetailRepository.findByCrn(crn: String): DetailPerson =
    getByCrn(crn) ?: throw NotFoundException("person", "crn", crn)

fun matchesForename(forename: String) = Specification<DetailPerson> { person, _, cb ->
    cb.equal(cb.lower(person[FORENAME]), forename.lowercase())
}

fun matchesSurname(surname: String) = Specification<DetailPerson> { person, _, cb ->
    cb.equal(cb.lower(person[SURNAME]), surname.lowercase())
}

fun matchesDateOfBirth(dob: LocalDate) =
    Specification<DetailPerson> { person, _, cb -> cb.equal(person.get<LocalDate>(DOB), dob) }

fun matchesCrn(crn: String) =
    Specification<DetailPerson> { person, _, cb -> cb.equal(person.get<String>(CRN), crn.uppercase()) }

fun matchesNomsId(nomsId: String) =
    Specification<DetailPerson> { person, _, cb -> cb.equal(person.get<String>(NOMS), nomsId.uppercase()) }

fun matchesPnc(pnc: String) =
    Specification<DetailPerson> { person, _, cb -> cb.equal(person.get<String>(PNC), pnc.uppercase()) }