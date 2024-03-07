package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "offender")
@EntityListeners(AuditingEntityListener::class)
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "second_name")
    val secondName: String?,

    @Column(name = "third_name")
    val thirdName: String?,

    @Column(name = "surname")
    val surname: String,

    @Column(columnDefinition = "char(7)")
    var nomsNumber: String? = null,

    @Column
    val croNumber: String? = null,

    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
)

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,

    @Column(name = "code_value")
    val code: String

) {
    fun prisonGenderCode() = when (code) {
        "M", "F" -> code
        "N" -> "NK"
        else -> "ALL"
    }
}

interface PersonRepository : JpaRepository<Person, Long> {
    @Query(
        """
        select p as person, d.startDate as sentenceDate, c as custody 
        from Custody c
        join c.disposal d 
        join d.event e
        join e.person p
        join fetch p.gender
        where p.crn = :crn 
        and p.softDeleted = false
        and d.softDeleted = false
        and d.active = true
        and e.softDeleted = false
        and e.active = true
        and c.softDeleted = false
        and c.bookingRef is null
    """
    )
    fun findByCrn(crn: String): List<SentencedPerson>
    fun findByNomsNumberAndSoftDeletedIsFalse(nomsNumber: String): Person?

    @Query("select p.crn from Person p where p.softDeleted = false")
    fun findAllCrns(): List<String>
}

interface SentencedPerson {
    val person: Person
    val sentenceDate: LocalDate
    val custody: Custody
}
