package uk.gov.justice.digital.hmpps.epf.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    val convictionDate: LocalDate?,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @Column(name = "disposal_date", nullable = false)
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "event_id", updatable = false)
    val event: Event,

    @OneToOne(mappedBy = "disposal")
    val custody: Custody? = null,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class Custody(

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class KeyDate(

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "key_date_id")
    val id: Long
) {
    enum class Type(val code: String) {
        EXPECTED_RELEASE_DATE("EXP")
    }
}

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        select e from Event e
        join fetch e.person p
        left join fetch e.disposal d
        left join fetch d.custody c
        where e.number = :number
        and p.crn = :crn
    """
    )
    fun findEventByCrnAndEventNumber(crn: String, number: String): Event?
}

fun EventRepository.getEvent(crn: String, number: String) =
    findEventByCrnAndEventNumber(crn, number) ?: throw NotFoundException("Event", "crn", crn)

interface KeyDateRepository : JpaRepository<KeyDate, Long> {
    @EntityGraph(attributePaths = ["type"])
    fun findByCustodyIdAndTypeCode(custodyId: Long, typeCode: String): KeyDate?
}

fun KeyDateRepository.getExpectedReleaseDate(custodyId: Long) =
    findByCustodyIdAndTypeCode(custodyId, KeyDate.Type.EXPECTED_RELEASE_DATE.code)
