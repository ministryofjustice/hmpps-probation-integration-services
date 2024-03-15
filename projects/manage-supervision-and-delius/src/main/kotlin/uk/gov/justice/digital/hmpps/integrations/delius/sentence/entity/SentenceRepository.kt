package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event



interface EventSentenceRepository : JpaRepository<Event, Long> {
    @Query(
        "SELECT e FROM Event e " +
            "JOIN Person p ON p.id = e.personId " +
            "LEFT JOIN FETCH e.court c " +
            "LEFT JOIN FETCH e.mainOffence m " +
            "LEFT JOIN FETCH e.additionalOffences ao " +
            "LEFT JOIN FETCH m.offence mo " +
            "LEFT JOIN FETCH ao.offence aoo " +
            "WHERE p.crn = :crn " +
            "AND e.active = true "
    )
    fun findActiveSentencesByCrn(crn: String): List<Event>
}


interface CourtRepository: JpaRepository<CourtAppearance, Long> {
    fun getFirstCourtAppearanceByEventIdOrderByDate(id: Long): CourtAppearance?
}
