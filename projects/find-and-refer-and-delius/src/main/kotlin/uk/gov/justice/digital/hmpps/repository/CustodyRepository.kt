package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.CustodialStatusCode
import uk.gov.justice.digital.hmpps.entity.Custody

interface CustodyRepository : JpaRepository<Custody, Long> {
    @Query(
        """
            select count(c) from Custody c
            join Disposal d on d.id = c.disposal.id and d.active = true and d.softDeleted = false 
            where d.event.person.id = :personId
            and c.status.code in (:statusCodes) 
        """
    )
    fun isInCustodyCount(personId: Long, statusCodes: List<String>): Int?
}

fun CustodyRepository.isInCustody(personId: Long) =
    (isInCustodyCount(personId, CustodialStatusCode.entries.map { it.code }) ?: 0) > 0