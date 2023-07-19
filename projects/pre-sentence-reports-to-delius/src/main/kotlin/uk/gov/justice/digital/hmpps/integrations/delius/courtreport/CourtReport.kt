package uk.gov.justice.digital.hmpps.integrations.delius.courtreport

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Immutable
@Entity
@Where(clause = "soft_deleted = 0")
class CourtReport(

    @Id
    @Column(name = "court_report_id")
    val id: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "court_appearance_id")
    val appearance: CourtAppearance,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Where(clause = "soft_deleted = 0")
class CourtAppearance(
    @Id
    @Column(name = "court_appearance_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Where(clause = "active_flag = 1 and soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)
