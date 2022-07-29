package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Immutable
@Entity
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    var person: Person,

    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    var active: Boolean,
)
