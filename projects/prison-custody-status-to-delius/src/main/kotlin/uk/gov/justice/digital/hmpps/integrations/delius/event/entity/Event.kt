package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.ZonedDateTime

@Entity
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @OneToOne(mappedBy = "event")
    var disposal: Disposal? = null,

    @Column(name = "active_flag", updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column
    var firstReleaseDate: ZonedDateTime? = null,

    @OneToMany(mappedBy = "event")
    val managers: List<OrderManager> = listOf()
) {
    fun manager() = managers.first()
}

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        select e from Event e
        where e.person.id = :personId 
        and e.person.softDeleted = false
        and e.disposal.type.sentenceType in ('NC', 'SC')
        and e.disposal.active = true and e.active = true
        and e.disposal.softDeleted = false and e.softDeleted = false
        and e.disposal.custody.status.code not in ('AT', 'T')
        """
    )
    fun findActiveCustodialEvents(personId: Long): List<Event>

    @Modifying
    @Query(
        """
        merge into iaps_event using dual on (event_id = ?1) 
        when matched then update set iaps_flag = ?2 
        when not matched then insert(event_id, iaps_flag) values(?1, ?2)
        """,
        nativeQuery = true
    )
    fun updateIaps(eventId: Long, iapsFlagValue: Long = 1)
}
