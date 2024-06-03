package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.util.*

@NamedEntityGraph(
    name = "person-with-ref-data",
    attributeNodes = [
        NamedAttributeNode("title"),
        NamedAttributeNode("gender"),
        NamedAttributeNode("nationality"),
        NamedAttributeNode("ethnicity")
    ]
)
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
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(value = "person-with-ref-data")
    fun findByCrn(crn: String): Person?

    @EntityGraph(value = "person-with-ref-data")
    override fun findById(id: Long): Optional<Person>

    @EntityGraph(value = "person-with-ref-data")
    override fun findAll(pageable: Pageable): Page<Person>
}

fun PersonRepository.getByCrn(crn: String): Person =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

fun PersonRepository.getByPersonId(id: Long): Person =
    findById(id).orElseThrow { NotFoundException("Person", "id", id) }