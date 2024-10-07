package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class Event(

    @Column(name = "event_number")
    val number: String,

    @Column(name = "offender_id")
    val personId: Long,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

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

    @Query(
        """
        select e.number from Event e
        where e.personId = :personId 
        and e.disposal.type.sentenceType in ('NC', 'SC')
        and e.disposal.active = true and e.active = true
        and e.disposal.softDeleted = false and e.softDeleted = false
        """
    )
    fun findActiveCustodialEvents(personId: Long): List<String>
}

fun EventRepository.getByNumber(personId: Long, number: String) =
    findByPersonIdAndNumber(personId, number)
        ?: throw IgnorableMessageException("Event with number of $number not found")
