package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Immutable
@Entity
@Table(name = "rqmnt")
class Requirement(
    @Id
    @Column(name = "rqmnt_id", nullable = false)
    val id: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disposal_id", nullable = false)
    val disposal: Disposal,

    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    val active: Boolean,
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
