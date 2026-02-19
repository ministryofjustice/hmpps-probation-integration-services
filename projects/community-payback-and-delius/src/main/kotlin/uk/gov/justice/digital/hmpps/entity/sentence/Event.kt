package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.sentence.EventIdRepositoryImpl.EventId
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

interface EventRepository : JpaRepository<Event, Long>, EventIdRepository {
    fun findByPersonIdAndNumberAndSoftDeletedIsFalse(personId: Long, eventNumber: String): Event?

    fun getByPersonAndEventNumber(personId: Long, eventNumber: String) =
        findByPersonIdAndNumberAndSoftDeletedIsFalse(personId, eventNumber)
            ?: throw NotFoundException("Event", "event number", eventNumber)

    fun getEventIds(pairs: List<Pair<String, Int>>) = findAllByPersonAndEventNumber(pairs)
        .associate { (crn, eventNumber, eventId) -> (crn to eventNumber) to eventId }

    fun getByPerson(person: Person): MutableList<Event>
    fun getByPerson_Id(personId: Long): MutableList<Event>
}

interface EventIdRepository {
    fun findAllByPersonAndEventNumber(pairs: Collection<Pair<String, Int>>): List<EventId>
}

// JPA can't handle lists of tuples as query parameters, so we use JDBC template here
class EventIdRepositoryImpl(private val jdbcTemplate: NamedParameterJdbcTemplate) : EventIdRepository {
    data class EventId(val crn: String, val eventNumber: Int, val eventId: Long)

    override fun findAllByPersonAndEventNumber(pairs: Collection<Pair<String, Int>>): List<EventId> =
        pairs.chunked(500).flatMap { params ->
            return jdbcTemplate.query(
                """
                    select event_id, crn, event_number
                    from event
                    join offender on offender.offender_id = event.offender_id
                    where (offender.crn, event.event_number) in (:values)
                    and event.soft_deleted = 0
                """.trimIndent(),
                MapSqlParameterSource().addValue("values", params.map { arrayOf<Any>(it.first, it.second) })
            ) { resultSet, _ ->
                EventId(
                    crn = resultSet.getString("crn"),
                    eventNumber = resultSet.getInt("event_number"),
                    eventId = resultSet.getLong("event_id"),
                )
            }
        }
}