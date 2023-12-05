package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface CustodyRepository : JpaRepository<Custody, Long> {
    @EntityGraph(attributePaths = ["status.dataset", "disposal.event.manager", "keyDates.type"])
    fun findCustodyById(id: Long): Custody

    @Query(
        """
        select c.id from Custody c
        join c.disposal d
        join d.event e
        where e.person.id = :personId
        and c.softDeleted = false and c.status.code <> 'P' and c.bookingRef = :bookingRef
        and d.active = true and d.softDeleted = false
        and e.active = true and e.softDeleted = false 
    """
    )
    fun findCustodyId(personId: Long, bookingRef: String): List<Long>

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select c.id from Custody c")
    fun findForUpdate(id: Long): Long
}
