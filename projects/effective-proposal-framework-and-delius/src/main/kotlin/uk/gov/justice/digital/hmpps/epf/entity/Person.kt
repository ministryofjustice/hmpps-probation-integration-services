package uk.gov.justice.digital.hmpps.epf.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Immutable
@Table(name = "offender")
@Entity
@SQLRestriction("soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String?,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentExclusion: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentRestriction: Boolean,

    @Column(name = "dynamic_rsr_score", columnDefinition = "number(5,2)")
    val dynamicRsrScore: Double?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false

)

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["gender"])
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
