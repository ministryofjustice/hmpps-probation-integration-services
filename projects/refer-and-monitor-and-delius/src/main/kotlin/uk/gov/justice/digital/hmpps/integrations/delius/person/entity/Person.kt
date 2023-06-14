package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "offender")
@Where(clause = "soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String? = null,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?

    @Query("select p.nomsId from Person p where p.crn = :crn")
    fun findNomsId(crn: String): String?
}

fun PersonRepository.getByCrn(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
