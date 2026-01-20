package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

@Entity
@Table(name = "event")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,

    var ftcCount: Long,

    val breachEnd: LocalDate?,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonIdAndNumberAndSoftDeletedIsFalse(personId: Long, eventNumber: String): Event?
}

fun EventRepository.getByPersonAndEventNumber(personId: Long, eventNumber: String) =
    findByPersonIdAndNumberAndSoftDeletedIsFalse(personId, eventNumber)
        ?: throw NotFoundException("Event", "event number", eventNumber)