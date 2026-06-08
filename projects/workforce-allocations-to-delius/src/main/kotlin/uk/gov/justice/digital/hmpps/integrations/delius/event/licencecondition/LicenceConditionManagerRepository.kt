package uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface LicenceConditionManagerRepository : JpaRepository<LicenceConditionManager, Long> {
    @Query("""
    select lm from LicenceConditionManager lm 
    where lm.licenceConditionId = :licenceConditionId 
    and lm.startDate <= :dateTime 
    and (lm.endDate is null or lm.endDate > :dateTime)  
    and lm.softDeleted = false
    """
    )
    fun findActiveManagerAtDate(licenceConditionId: Long, dateTime: ZonedDateTime): LicenceConditionManager?
}