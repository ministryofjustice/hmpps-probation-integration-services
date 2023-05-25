package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.responsibleofficer

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long> {

    @Query(
        """
            select ro from ResponsibleOfficer ro
            where ro.personId = :personId
            and ro.startDate <= :date
            and (ro.endDate is null or ro.endDate > :date)
        """
    )
    fun findActiveManagerAtDate(personId: Long, date: ZonedDateTime): ResponsibleOfficer?
}
