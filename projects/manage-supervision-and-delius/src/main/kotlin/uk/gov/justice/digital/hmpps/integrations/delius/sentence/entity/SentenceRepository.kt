package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import java.time.LocalDate

interface CourtDocumentDetails {
    val id: Long
    val lastSaved: LocalDate
    val description: String
}
interface EventSentenceRepository : JpaRepository<Event, Long> {
    @Query(
        "SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.disposal d " +
            "LEFT JOIN FETCH d.type t  " +
            "LEFT JOIN FETCH e.court c " +
            "LEFT JOIN FETCH e.mainOffence m " +
            "LEFT JOIN FETCH e.additionalOffences ao " +
            "LEFT JOIN FETCH m.offence mo " +
            "LEFT JOIN FETCH ao.offence aoo " +
            "WHERE e.personId = :id " +
            "AND e.active = true " +
            "ORDER BY e.eventNumber DESC "
    )
    fun findActiveSentencesByPersonId(id: Long): List<Event>

    @Query(
        """
        SELECT DOCUMENT_ID, LAST_SAVED, ITEM FROM (
            SELECT d.DOCUMENT_ID, d.LAST_SAVED, NVL(DECODE(d.DOCUMENT_TYPE, 'CPS_PACK', 'CPS Pack', rdt.DESCRIPTION), 'Pre Sentence Event') AS ITEM
            FROM DOCUMENT d 
            JOIN EVENT e 
            ON e.EVENT_ID = d.PRIMARY_KEY_ID 
            LEFT JOIN DISPOSAL d2 
            ON d2.EVENT_ID = e.EVENT_ID 
            LEFT JOIN R_DISPOSAL_TYPE rdt 
            ON rdt.DISPOSAL_TYPE_ID = d2.DISPOSAL_TYPE_ID 
            WHERE d.OFFENDER_ID = :personId
            AND e.event_number = :eventNumber
            AND TABLE_NAME = 'EVENT'
            UNION 
            SELECT d.DOCUMENT_ID, d.LAST_SAVED, rcrt.DESCRIPTION AS item
            FROM DOCUMENT d 
            JOIN COURT_REPORT cr 
            ON cr.COURT_REPORT_ID = d.PRIMARY_KEY_ID 
            JOIN COURT_APPEARANCE ca 
            ON ca.COURT_APPEARANCE_ID = cr.COURT_APPEARANCE_ID 
            JOIN R_COURT_REPORT_TYPE rcrt 
            ON rcrt.COURT_REPORT_TYPE_ID = cr.COURT_REPORT_TYPE_ID 
            JOIN EVENT e 
            ON e.EVENT_ID = ca.EVENT_ID 
            WHERE d.OFFENDER_ID = :personId
            AND e.event_number = :eventNumber
            AND TABLE_NAME = 'COURT_REPORT'   
        )
        ORDER BY last_saved DESC
        """, nativeQuery = true
    )
    fun getCourtDocuments(personId: Long, eventNumber: String): List<CourtDocumentDetails>
}

interface CourtAppearanceRepository : JpaRepository<CourtAppearance, Long> {
    fun getFirstCourtAppearanceByEventIdOrderByDate(id: Long): CourtAppearance?
}

interface AdditionalSentenceRepository : JpaRepository<AdditionalSentence, Long> {
    fun getAllByEventId(id: Long): List<AdditionalSentence>
}

