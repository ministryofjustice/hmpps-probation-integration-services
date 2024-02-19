package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class Event(

    @Column(name = "event_number")
    val number: String,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long
)

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonIdAndNumber(personId: Long, number: String): Event?
}

fun EventRepository.getByNumber(personId: Long, number: String) =
    findByPersonIdAndNumber(personId, number) ?: throw NotFoundException("Event", "number", number)
