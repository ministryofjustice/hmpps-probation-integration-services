package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.LockModeType
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
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

    @Lock(LockModeType.PESSIMISTIC_READ)
    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?
}

fun PersonRepository.getByCrnForUpdate(crn: String) =
    findByCrnAndSoftDeletedIsFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
