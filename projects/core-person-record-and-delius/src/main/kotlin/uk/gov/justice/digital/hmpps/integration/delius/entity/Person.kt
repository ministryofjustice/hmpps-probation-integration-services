package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.util.*

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
data class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "nomsNumber", columnDefinition = "char(7)")
    val nomsId: String?,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

    @Column(name = "cro_number", columnDefinition = "char(12)")
    val cro: String?,

    @Column(name = "ni_number", columnDefinition = "char(9)")
    val niNumber: String?,

    @Column(name = "most_recent_prisoner_number")
    val prisonerNumber: String?,

    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dob: LocalDate,
    val previousSurname: String?,
    val preferredName: String?,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    @Column(name = "e_mail_address")
    val emailAddress: String?,

    @OneToOne(mappedBy = "person", optional = false)
    val personManager: PersonManager?,

    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    val nationality: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData?,

    val ethnicityDescription: String?,
    val exclusionMessage: String?,
    val restrictionMessage: String?,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {
    @Query(
        """
            select p from Person p
            join fetch p.personManager pm
            join fetch pm.probationArea pa
            left join fetch p.gender
            left join fetch p.ethnicity
            left join fetch p.nationality
            where p.crn = :crn and pa.code <> 'XXX' and pm.softDeleted = false and pm.active = true
        """
    )
    fun findByCrn(crn: String): Person?

    @Query(
        """
            select p from Person p
            join fetch p.personManager pm
            join fetch pm.probationArea pa
            left join fetch p.gender
            left join fetch p.ethnicity
            left join fetch p.nationality
            where p.id = :id and pa.code <> 'XXX' and pm.softDeleted = false and pm.active = true
        """
    )
    override fun findById(id: Long): Optional<Person>

    @Query(
        """
            select p from Person p
            join fetch p.personManager pm
            join fetch pm.probationArea pa
            left join fetch p.gender
            left join fetch p.ethnicity
            left join fetch p.nationality
            where pa.code <> 'XXX' and pm.softDeleted = false and pm.active = true
        """
    )
    override fun findAll(pageable: Pageable): Page<Person>
}

fun PersonRepository.getByCrn(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

fun PersonRepository.getByPersonId(id: Long): Person =
    findById(id).orElseThrow { NotFoundException("Person", "id", id) }