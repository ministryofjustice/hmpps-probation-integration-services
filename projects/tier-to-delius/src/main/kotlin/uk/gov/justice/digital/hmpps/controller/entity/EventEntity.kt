package uk.gov.justice.digital.hmpps.controller.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Where(clause = "softDeleted = 0 and active = 1")
class EventEntity(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: CaseEntity,

    @OneToOne(mappedBy = "eventEntity")
    val disposal: Disposal? = null,

    @OneToOne(mappedBy = "eventEntity")
    val mainOffence: MainOffence? = null,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean,

    @Column(name = "in_breach")
    var inBreach: Boolean = false,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean
)

@Immutable
@Entity
@Where(clause = "softDeleted = 0 and active = 1")
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", updatable = false)
    val eventEntity: EventEntity,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", updatable = false)
    val disposalType: DisposalType,

    @OneToMany(mappedBy = "disposal")
    val requirements: List<RequirementEntity>,

    @Column(name = "disposal_date", nullable = false)
    val disposalDate: ZonedDateTime,

    val terminationDate: LocalDate? = null,

    @Column(name = "active_flag", updatable = false, columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
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
    val eventEntity: EventEntity,

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
    val mainCategoryDescription: String
)

interface EventRepository : JpaRepository<EventEntity, Long> {

    @Query(
        """
        select e from EventEntity e 
        where e.person.crn = :crn
        and e.softDeleted = false
    """
    )
    fun findByCrn(crn: String): List<EventEntity>

}
