package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.LockModeType
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.util.Optional

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
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

    fun findByCrn(crn: String): Person?

    @Lock(LockModeType.PESSIMISTIC_READ)
    override fun findById(personId: Long): Optional<Person>
}

fun PersonRepository.getByCrn(crn: String) =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

fun PersonRepository.getByIdForUpdate(personId: Long) =
    findById(personId).orElseThrow { NotFoundException("Person", "id", personId) }
