package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
class Event(

    @Column(name = "event_number", nullable = false)
    val number: String,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long
)

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonIdAndNumber(personId: Long, number: String): Event?
}

fun EventRepository.getByPersonIdAndNumber(personId: Long, number: String) =
    findByPersonIdAndNumber(personId, number) ?: throw NotFoundException("Event", "number", number)