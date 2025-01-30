package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person

interface EventSentenceRepository : JpaRepository<Event, Long> {
    @Query(
        """
            SELECT e FROM Event e
            LEFT JOIN FETCH e.disposal d 
            LEFT JOIN FETCH d.type t 
            LEFT JOIN FETCH e.court c
            LEFT JOIN FETCH e.mainOffence m 
            LEFT JOIN FETCH e.additionalOffences ao 
            LEFT JOIN FETCH m.offence mo
            LEFT JOIN FETCH ao.offence aoo 
            WHERE e.personId = :id
            ORDER BY e.dateCreated DESC 
        """
    )
    fun findSentencesByPersonId(id: Long): List<Event>

    fun findEventByPersonIdAndEventNumberAndActiveIsFalse(id: Long, eventNumber: String): Event?

    fun findEventByPersonIdAndEventNumberAndActiveIsTrue(id: Long, eventNumber: String): Event?
}

fun EventSentenceRepository.getEvent(id: Long, eventNumber: String) =
    findEventByPersonIdAndEventNumberAndActiveIsTrue(id, eventNumber) ?: throw NotFoundException(
        "Event",
        "number",
        eventNumber
    )

interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {
    fun getFirstCourtAppearanceByEventIdOrderByDate(id: Long): CourtAppearance?
}

interface AdditionalSentenceRepository : JpaRepository<AdditionalSentence, Long> {
    fun getAllByEventId(id: Long): List<AdditionalSentence>
}

interface OffenderManagerRepository : JpaRepository<OffenderManager, Long> {

    fun findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(crn: String): OffenderManager?

    fun countOffenderManagersByPerson(person: Person): Long

    @Query(
        """
        SELECT om
        FROM OffenderManager om
        WHERE om.person.id = :id 
        ORDER BY om.endDate
    """
    )
    fun findOffenderManagersByPersonOrderByEndDateDesc(id: Long): List<OffenderManager>
}

fun OffenderManagerRepository.getByCrn(crn: String) =
    findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(crn) ?: throw NotFoundException("Person", "crn", crn)


