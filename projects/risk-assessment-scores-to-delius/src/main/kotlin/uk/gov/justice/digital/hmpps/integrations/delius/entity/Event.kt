package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.LockModeType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence? = null
)

@Immutable
@Entity
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", updatable = false)
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", updatable = false)
    val disposalType: DisposalType,

    @Column(name = "active_flag", updatable = false, columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(name = "disposal_date", nullable = false)
    val disposalDate: ZonedDateTime
)

@Immutable
@Entity
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "description")
    val description: String
)

@Immutable
@Entity
@Table(name = "main_offence")
@Where(clause = "soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(name = "offence_date")
    val date: LocalDate,

    @Column(name = "offence_count")
    val offenceCount: Int,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,
    @Column(name = "main_category_code", columnDefinition = "char(3)")
    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    @Column(name = "sub_category_code", columnDefinition = "char(2)")
    val subCategoryCode: String,
    val subCategoryDescription: String
)

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        select e from Event e 
        where e.person.crn = :crn
        and e.number = :eventNumber
        and e.softDeleted = false
    """
    )
    fun findByCrn(crn: String, eventNumber: String): Event?
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select e.id from Event e where e.id = : id")
    fun findForUpdate(id: Long): Long
}
