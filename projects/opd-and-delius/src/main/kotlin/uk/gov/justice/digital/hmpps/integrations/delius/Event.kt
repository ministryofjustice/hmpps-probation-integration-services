package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean,
    @Id
    @Column(name = "event_id")
    val id: Long,
)

interface EventRepository : JpaRepository<Event, Long> {
    fun existsByPersonId(personId: Long): Boolean
}
