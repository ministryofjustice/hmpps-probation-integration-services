package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

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
    val additionalOffences: List<AdditionalOffence> = emptyList(),

    @Column(name = "notes")
    val notes: String,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        "SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.disposal d " +
            "LEFT JOIN FETCH d.type t  " +
            "LEFT JOIN FETCH e.mainOffence m " +
            "LEFT JOIN FETCH e.additionalOffences ao " +
            "LEFT JOIN FETCH m.offence mo " +
            "LEFT JOIN FETCH ao.offence aoo " +
            "WHERE e.personId = :personId"
    )
    fun findByPersonId(personId: Long): List<Event>
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

    @Column(name = "offence_count")
    val offenceCount: Long,

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Column(name = "offence_date")
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "additional_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(
    @Id
    @Column(name = "additional_offence_id")
    val id: Long,

    @Column(name = "offence_count")
    val offenceCount: Long,

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

)

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


