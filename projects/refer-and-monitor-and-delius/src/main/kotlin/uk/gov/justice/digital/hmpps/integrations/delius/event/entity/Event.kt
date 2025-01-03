package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "event")
class Event(

    @Column(name = "offender_id")
    val personId: Long,

    val convictionDate: LocalDate?,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(name = "ftc_count")
    var ftcCount: Long,

    @Column(name = "in_breach", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val inBreach: Boolean,

    @Column(name = "breach_end")
    val breachEnd: LocalDate?,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "main_offence")
class MainOffence(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "main_offence_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_offence")
class Offence(
    val mainCategoryDescription: String,
    val subCategoryDescription: String,
    @Id
    @Column(name = "offence_id")
    val id: Long
)

interface EventRepository : JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = ["disposal.type", "mainOffence.offence"])
    override fun findById(id: Long): Optional<Event>

    @Query(
        """
        select e from Event e
        join fetch e.disposal d
        join fetch d.type
        join fetch e.mainOffence mo
        join fetch mo.offence
        join Person p on p.id = e.personId
        where p.crn = :crn
        and e.active = true and e.softDeleted = false
        and d.active = true and d.softDeleted = false
        and mo.softDeleted = false
        and d.type.code <> '326' 
    """
    )
    fun findAllByCrn(crn: String): List<Event>

    @Query(
        """
        select e from Event e
        join fetch e.disposal d
        join fetch d.type
        join fetch e.mainOffence mo
        join fetch mo.offence
        join Person p on p.id = e.personId
        where p.crn = :crn and e.id = :id
    """
    )
    fun findByCrnAndId(crn: String, id: Long): Event?
}

fun EventRepository.getByCrnAndId(crn: String, id: Long) =
    findByCrnAndId(crn, id) ?: throw NotFoundException("Event $id not found for $crn")
