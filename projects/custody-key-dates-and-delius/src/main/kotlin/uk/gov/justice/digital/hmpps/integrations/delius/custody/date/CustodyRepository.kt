package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CustodyRepository : JpaRepository<Custody, Long> {
    @Query(
        """
        SELECT c FROM Custody c
        JOIN FETCH c.disposal d
        JOIN FETCH d.event e
        JOIN FETCH e.manager
        LEFT OUTER JOIN FETCH c.keyDates
        WHERE e.person.id = :personId
        AND c.softDeleted = false AND c.status.code <> 'P' AND c.bookingRef = :bookingRef
        AND e.active = true AND e.softDeleted = false 
        AND d.active = true and d.softDeleted = false
    """,
    )
    fun findCustody(
        personId: Long,
        bookingRef: String,
    ): List<Custody>
}
