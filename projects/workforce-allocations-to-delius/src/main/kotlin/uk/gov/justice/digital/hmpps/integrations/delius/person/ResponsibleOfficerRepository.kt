package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long> {
    @Query(
        """
      SELECT ro FROM ResponsibleOfficer ro 
      WHERE ro.personId = :personId
      and ro.startDate <= :dateTime AND (ro.endDate IS NULL OR ro.endDate > :dateTime)
      """,
    )
    fun findActiveManagerAtDate(
        personId: Long,
        dateTime: ZonedDateTime,
    ): ResponsibleOfficer?
}
