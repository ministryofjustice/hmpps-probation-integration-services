package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface PrisonManagerRepository : JpaRepository<PrisonManager, Long> {
    @Query(
        """
            select pm from PrisonManager pm
            where pm.personId = :personId
            and pm.softDeleted = false
            and pm.date <= :date
            and (pm.endDate is null or pm.endDate > :date)
        """
    )
    fun findActiveManagerAtDate(personId: Long, date: ZonedDateTime): PrisonManager?
}
