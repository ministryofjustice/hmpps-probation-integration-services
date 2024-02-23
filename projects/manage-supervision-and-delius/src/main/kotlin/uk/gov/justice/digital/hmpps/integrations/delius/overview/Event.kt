package uk.gov.justice.digital.hmpps.integrations.delius.overview

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate
import java.util.*
import java.util.Collections.emptySortedSet

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "event_number")
    val eventNumber: String,

    @Column(name = "in_breach", columnDefinition = "number")
    val inBreach: Boolean,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @OneToOne(mappedBy = "event")
    val mainOffence: MainOffence? = null,

    @OneToMany(mappedBy = "event")
    val additionalOffences: SortedSet<AdditionalOffence> = emptySortedSet(),

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
) : Comparable<Event> {
    override fun compareTo(other: Event): Int {
        val eventNum = -eventNumber.compareTo(other.eventNumber)
        if(eventNum == 0){
            return -id.compareTo(other.id)
        }
        return eventNum
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Provision

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

interface EventRepository : JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = ["mainOffence","additionalOffences","additionalOffences.offence","mainOffence.offence"])
    @Query("SELECT e FROM Event e " +
        "LEFT JOIN FETCH e.disposal d " +
        "LEFT JOIN FETCH d.type t  " +
        "WHERE e.personId = :personId and e.active = true")
    fun findByPersonId(personId: Long): List<Event>

    @Query("SELECT count(*) FROM Event e INNER JOIN Disposal d ON d.event.id = e.id " +
           "WHERE e.personId = :personId and e.active = false")
    fun getInactiveEventsWithDisposals(personId: Long): Int

    @Query("SELECT count(*) FROM Event e INNER JOIN Disposal d ON d.event.id = e.id " +
        "WHERE e.personId = :personId and e.active = false and e.inBreach = true")
    fun getInactiveEventsWithDisposalsBreached(personId: Long): Int

}

@Entity
@Immutable
@Table(name = "disposal")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "disposal_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "entered_notional_end_date")
    val enteredEndDate: LocalDate? = null,

    @Column(name = "notional_end_date")
    val notionalEndDate: LocalDate? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
) {
    fun expectedEndDate() = enteredEndDate ?: notionalEndDate
}

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(

    @Column(name = "disposal_type_code")
    val code: String,

    val description: String,

    @Column(name = "sentence_type")
    val sentenceType: String? = null,

    @Column(name = "ftc_limit")
    val ftcLimit: Long? = null,

    @Id
    @Column(name = "disposal_type_id")
    val id: Long
)

@Immutable
@Table(name = "main_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event?,

    @Column(name = "offence_date")
    val date: LocalDate,

    @JoinColumn(name = "offence_id")
    @ManyToOne(cascade = [CascadeType.PERSIST])
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)
interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    @EntityGraph(attributePaths = ["offence"])
    fun findByEvent(event: Event): MainOffence?
}

@Immutable
@Table(name = "additional_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event?,

    @Column(name = "offence_date")
    val date: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false

) : Comparable<AdditionalOffence> {
    override fun compareTo(other: AdditionalOffence): Int {
        val eventNum = date.zeroed().compareTo(other.date.zeroed())
        if(eventNum == 0){
            return -id.compareTo(other.id)
        }
        return eventNum
    }
    private fun LocalDate?.zeroed() = this ?: LocalDate.MIN
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Provision

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Immutable
@Table(name = "r_offence")
@Entity
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Column
    val description: String
)


