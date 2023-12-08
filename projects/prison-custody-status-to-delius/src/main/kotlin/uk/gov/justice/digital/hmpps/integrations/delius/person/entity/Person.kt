package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "offender")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val nomsNumber: String,
    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByNomsNumberAndSoftDeletedIsFalse(nomsNumber: String): List<Person>
}
