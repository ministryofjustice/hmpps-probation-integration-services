package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
@Table(name = "offender")
@Where(clause = "soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "second_name")
    val secondName: String?,

    @Column(name = "third_name")
    val thirdName: String?,

    @Column(name = "surname")
    val surname: String,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomisId: String?,

    @OneToMany(mappedBy = "person")
    val events: List<Event>,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["events.disposal", "events.mainOffence"])
    fun findByNomisId(nomisId: String): Person?
}
