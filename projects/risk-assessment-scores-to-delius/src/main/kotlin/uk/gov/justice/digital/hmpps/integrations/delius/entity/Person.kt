package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val crn: String,
    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String? = null,
    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,
    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,
    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(name = "surname", length = 35)
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getByCrn(crn: String) =
    findByCrn(crn.uppercase()) ?: throw NotFoundException("Person", "crn", crn)
