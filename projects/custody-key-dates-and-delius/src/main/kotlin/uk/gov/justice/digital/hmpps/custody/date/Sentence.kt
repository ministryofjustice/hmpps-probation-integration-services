package uk.gov.justice.digital.hmpps.custody.date

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.custody.date.reference.ReferenceData

@Immutable
@Entity
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    val active: Boolean,

    val eventNumber: String,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @OneToOne(mappedBy = "event")
    val manager: OrderManager,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
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

    @Column(name = "active_flag", updatable = false, columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val status: ReferenceData,

    @Column(name = "prisoner_number")
    val bookingRef: String,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal? = null,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = listOf(),

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
@Where(clause = "active_flag = 1")
class OrderManager(
    @Id
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "probation_area_id")
    val providerId: Long,

    @Column(name = "allocation_team_id")
    val teamId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,
)
