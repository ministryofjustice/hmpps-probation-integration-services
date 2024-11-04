package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
@Table(name = "offender")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column
    val softDeleted: Boolean = false,
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrn(crn: String): Person?
}

fun PersonRepository.getByCrn(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)