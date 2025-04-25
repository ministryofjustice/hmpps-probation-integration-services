package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.service.withinDays
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.stream.Stream

@Entity
@Table(name = "offender")
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
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

    @Column(name = "previous_surname")
    val previousSurname: String? = null,

    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,

    @Column(name = "current_disposal", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentDisposal: Boolean,

    @Column(columnDefinition = "char(7)")
    var nomsNumber: String? = null,

    @Column(name = "immigration_number")
    val immigrationNumber: String? = null,

    @Column(name = "ni_number", columnDefinition = "char(9)")
    val niNumber: String? = null,

    @Column(name = "most_recent_prisoner_number")
    var mostRecentPrisonerNumber: String? = null,

    @Column
    val croNumber: String? = null,

    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,

    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    val events: List<Event>,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
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
) {
    fun isSentenced() = events.any { it.disposal != null }

    fun sentenceDates() = events.mapNotNull { it.disposal?.startDate }

    fun custodies() = events.mapNotNull { it.disposal?.custody }

    fun custodiesWithSentenceDateCloseTo(sentenceDate: LocalDate) =
        custodies().filter { sentenceDate.withinDays(it.disposal.startDate) }
}

@Immutable
@Entity
@Table(name = "alias")
@SQLRestriction("soft_deleted = 0")
class Alias(
    @Id
    @Column(name = "alias_id")
    val id: Long,
    @Column(name = "offender_id")
    val offenderId: Long,
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
    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Entity
@Immutable
class OffenderManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,
)

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
    """
    )
    fun findSentencedByCrn(crn: String): List<SentencedPerson>

    fun findByCrn(crn: String): Person?

    @Query("select p.crn from Person p where p.softDeleted = false")
    fun findAllCrns(): Stream<String>

    fun findAllByNomsNumberAndIdNot(nomsNumber: String, id: Long): List<Person>

    fun findAllByNomsNumber(nomsNumber: String): List<Person>
}

fun PersonRepository.getByCrn(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

interface SentencedPerson {
    val person: Person
    val sentenceDate: LocalDate?
    val custody: Custody?
}
