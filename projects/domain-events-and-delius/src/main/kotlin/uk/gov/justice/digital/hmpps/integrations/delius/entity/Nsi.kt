package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.annotations.Immutable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Immutable
@Entity
class Nsi(
    @Id
    @Column(name = "nsi_id")
    var id: Long,

    val referralDate: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "offender_id", updatable = false)
    val offender: Offender,

    @ManyToOne
    @JoinColumn(name = "event_id", updatable = false)
    val event: Event? = null,
)

@Immutable
@Entity
class Offender(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "CHAR(7)")
    val crn: String,
)

@Immutable
@Entity
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,
)
