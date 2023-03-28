package uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import java.time.ZonedDateTime

@Entity
@Immutable
class Disposal(
    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column
    val terminationDate: ZonedDateTime? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)
