package uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.host.entity.Host
import java.time.ZonedDateTime

interface HostRepository : JpaRepository<Host, Long> {
    @Query(
        """
        select host.probationAreaId from Host host
        where host.leadHost = true 
        and host.institutionId = :institutionId
        and host.startDate <= :date
        and (host.endDate is null or host.endDate >= :date)
        """
    )
    fun findLeadHostProviderIdByInstitutionId(institutionId: Long, date: ZonedDateTime): Long?
}
