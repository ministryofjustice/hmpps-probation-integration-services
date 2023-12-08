package uk.gov.justice.digital.hmpps.integrations.delius.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Immutable
@Entity
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,
    @Column(name = "event_number", nullable = false)
    val number: String,
    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,
    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean,
    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,
)
