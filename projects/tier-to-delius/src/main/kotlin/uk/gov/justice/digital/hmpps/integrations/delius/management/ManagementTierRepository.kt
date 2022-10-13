package uk.gov.justice.digital.hmpps.integrations.delius.management

import org.springframework.data.jpa.repository.JpaRepository

interface ManagementTierRepository : JpaRepository<ManagementTier, ManagementTierId> {
    fun findFirstByIdPersonIdOrderByIdDateChangedDesc(peronId: Long): ManagementTier?
}
