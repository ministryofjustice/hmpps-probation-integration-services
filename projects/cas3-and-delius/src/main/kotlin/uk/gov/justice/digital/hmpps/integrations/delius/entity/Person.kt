package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "offender")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "char(7)")
    val nomsNumber: String?,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {

    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?
    fun findByNomsNumberAndSoftDeletedIsFalse(nomsNumber: String): Person?
}

fun PersonRepository.getByCrn(crn: String) =
    findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)

fun PersonRepository.getByNoms(noms: String) =
    findByNomsNumberAndSoftDeletedIsFalse(noms) ?: throw NotFoundException("Person", "noms", noms)

