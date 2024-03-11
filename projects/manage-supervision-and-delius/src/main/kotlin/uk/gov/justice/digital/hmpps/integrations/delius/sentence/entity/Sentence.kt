package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event

interface EventSentenceRepository : JpaRepository<Event, Long> {

    @Query(
        "SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.disposal d " +
            "LEFT JOIN FETCH d.type t  " +
            "LEFT JOIN FETCH e.mainOffence m " +
            "LEFT JOIN FETCH e.additionalOffences ao " +
            "LEFT JOIN FETCH m.offence mo " +
            "LEFT JOIN FETCH ao.offence aoo " +
            "WHERE e.personId = :personId"
    )
    fun findPersonById(personId: Long): List<Event>
}

