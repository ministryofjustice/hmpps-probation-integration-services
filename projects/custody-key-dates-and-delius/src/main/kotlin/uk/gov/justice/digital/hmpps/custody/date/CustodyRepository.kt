package uk.gov.justice.digital.hmpps.custody.date

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CustodyRepository : JpaRepository<Custody, Long> {
    @Query(
        """
        SELECT c FROM Custody c
        JOIN c.disposal d
        JOIN d.event e
        WHERE e.person.id = :personId
        AND c.softDeleted = false AND c.status.code <> 'P' AND c.bookingRef = :bookingRef
        AND e.active = true AND e.softDeleted = false 
        AND d.active = true and d.softDeleted = false
    """
    )
    fun findCustody(personId: Long, bookingRef: String): List<Custody>
}
