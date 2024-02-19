package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Immutable
@Entity
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    val eventNumber: String,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    val active: Boolean = true,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @OneToOne(mappedBy = "event")
    @SQLRestriction("active_flag = 1 and soft_deleted = 0")
    val manager: OrderManager? = null,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
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
    val softDeleted: Boolean = false
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
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1")
@Table(name = "order_manager")
class OrderManager(
    @Id
    @Column(name = "order_manager_id")
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

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true
)
