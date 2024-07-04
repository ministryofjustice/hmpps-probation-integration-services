package uk.gov.justice.digital.hmpps.integrations.delius.management

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface ManagementTierRepository : JpaRepository<ManagementTier, ManagementTierId> {
    fun findFirstByIdPersonIdOrderByIdDateChangedDesc(personId: Long): ManagementTier?
}

interface ManagementTierWithEndDateRepository : JpaRepository<ManagementTierWithEndDate, ManagementTierId> {
    @Modifying
    @Query("update ManagementTierWithEndDate t set t.endDate = :endDate where t.id = :id")
    fun setEndDate(id: ManagementTierId, endDate: ZonedDateTime)
}