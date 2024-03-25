package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.ZonedDateTime

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @Query(
        """
        select pm from PersonManager pm 
        where pm.personId = :personId 
        and pm.startDate <= :dateTime 
        and (pm.endDate is null or pm.endDate > :dateTime)  
        and pm.softDeleted = false
        """
    )
    fun findActiveManager(personId: Long, dateTime: ZonedDateTime = ZonedDateTime.now()): PersonManager?
}

fun PersonManagerRepository.getActiveManager(personId: Long) = findActiveManager(personId)
    ?: throw NotFoundException("PersonManager", "personId", personId)
