package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    )

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonCrnAndNumber(crn: String, number: String): Event?
}

fun EventRepository.getByCrnAndNumber(crn: String, number: String): Event =
    findByPersonCrnAndNumber(crn, number) ?: throw NotFoundException("Event", "event number", number)

