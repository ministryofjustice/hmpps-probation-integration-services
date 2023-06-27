package uk.gov.justice.digital.hmpps.service.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.service.Person

@Entity
@Immutable
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @OneToOne(mappedBy = "event")
    var disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)
