package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import jakarta.persistence.criteria.JoinType
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person.Companion.ADDITIONAL_IDENTIFIERS
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person.Companion.ALIASES
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person.Companion.CRN
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person.Companion.FORENAME
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person.Companion.NOMS
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person.Companion.PNC
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person.Companion.SURNAME
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "second_name")
    val secondName: String?,

    @Column(name = "third_name")
    val thirdName: String?,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String?,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

    @Column(name = "cro_number")
    val cro: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentExclusion: Boolean?,
    val exclusionMessage: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentRestriction: Boolean?,
    val restrictionMessage: String?,

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

    @ManyToOne
    @JoinColumn(name = "sexual_orientation_id")
    val sexualOrientation: ReferenceData?,

    @Column(name = "telephone_number")
    val telephoneNumber: String?,

    @Column(name = "mobile_number")
    val mobileNumber: String?,

    @Column(name = "e_mail_address")
    val emailAddress: String?,

    @OneToMany(mappedBy = "person")
    val aliases: List<PersonAlias>,

    @OneToMany(mappedBy = "person")
    val additionalIdentifiers: List<AdditionalIdentifier>,

    @OneToMany(mappedBy = "person")
    val disabilities: List<Disability>,

    @Column(name = "current_disposal", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentDisposal: Boolean,

    @Column(name = "dynamic_rsr_score", columnDefinition = "number(5,2)")
    val dynamicRsrScore: Double?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long,
) {
    companion object {
        val CRN = Person::crn.name
        val FORENAME = Person::forename.name
        val SURNAME = Person::surname.name
        val DOB = Person::dateOfBirth.name
        val NOMS = Person::nomsId.name
        val PNC = Person::pnc.name
        val ALIASES = Person::aliases.name
        val ADDITIONAL_IDENTIFIERS = Person::additionalIdentifiers.name
    }
}

interface PersonRepository : JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
    @Query("select p.crn from Person p where p.nomsId = :nomsId and p.softDeleted = false")
    fun findByNomsId(nomsId: String): String?

    @Query(
        """
        select p from Person p 
        where exists (select 1 from RegistrationEntity r where r.personId = p.id
        and r.type.code = 'MAPP' and r.category.code in :mappaCategories)
        and p.crn = :crn
    """
    )
    fun findPersonInMappaCategory(crn: String, mappaCategories: Set<String>): Person?

    fun existsByCrn(crn: String): Boolean

    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getMappaPersonInMappaCategory(crn: String, mappaCategories: Set<String>) =
    findPersonInMappaCategory(crn, mappaCategories)
        ?: throw NotFoundException("Person with mappa cat in $mappaCategories", "crn", crn)

fun PersonRepository.getCrn(nomsId: String) =
    findByNomsId(nomsId) ?: throw NotFoundException("Person", "nomsId", nomsId)

fun PersonRepository.getByCrn(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

fun matchesPerson(firstName: String?, surname: String?, dateOfBirth: LocalDate?) =
    Specification<Person> { person, _, cb ->
        val toMatch = listOfNotNull(
            firstName?.lowercase()?.let { cb.equal(cb.lower(person[FORENAME]), it) },
            surname?.lowercase()?.let { cb.equal(cb.lower(person[SURNAME]), it) },
            dateOfBirth?.let { cb.equal(person.get<LocalDate>(Person.DOB), it) }
        )
        cb.and(*toMatch.toTypedArray())
    }

fun matchesAlias(firstName: String?, surname: String?, dateOfBirth: LocalDate?) =
    Specification<Person> { person, _, cb ->
        val alias = person.join<Person, PersonAlias>(ALIASES, JoinType.LEFT)
        val toMatch = listOfNotNull(
            firstName?.lowercase()?.let { cb.equal(cb.lower(alias[PersonAlias.FORENAME]), it) },
            surname?.lowercase()?.let { cb.equal(cb.lower(alias[PersonAlias.SURNAME]), it) },
            dateOfBirth?.let { cb.equal(alias.get<LocalDate>(PersonAlias.DOB), it) }
        )
        cb.and(*toMatch.toTypedArray())
    }

fun matchesCrnOrPrevious(crn: String) = Specification<Person> { person, _, cb ->
    val ai = person.join<Person, AdditionalIdentifier>(ADDITIONAL_IDENTIFIERS, JoinType.LEFT)
    val type = ai.join<AdditionalIdentifier, ReferenceData>(AdditionalIdentifier.TYPE, JoinType.LEFT)
    cb.or(
        cb.equal(person.get<String>(CRN), crn.uppercase()),
        cb.and(
            cb.equal(type.get<String>("code"), "MFCRN"),
            cb.equal(ai.get<String>(AdditionalIdentifier.IDENTIFIER), crn.uppercase())
        )
    )
}

fun matchesNomsId(nomsId: String) =
    Specification<Person> { person, _, cb -> cb.equal(person.get<String>(NOMS), nomsId.uppercase()) }

fun matchesPnc(pnc: String) =
    Specification<Person> { person, _, cb -> cb.equal(person.get<String>(PNC), pnc.uppercase()) }