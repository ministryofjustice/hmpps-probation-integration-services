package uk.gov.justice.digital.hmpps.integrations.delius.event.requirement

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table

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
