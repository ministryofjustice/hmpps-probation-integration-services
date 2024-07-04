package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTier
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierId
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierWithEndDate

interface ManagementTierDevRepository : JpaRepository<ManagementTier, ManagementTierId> {
    fun findByIdPersonId(personId: Long): ManagementTier
}

interface ManagementTierWithEndDateDevRepository : JpaRepository<ManagementTierWithEndDate, ManagementTierId> {
    fun findAllByIdPersonIdOrderByIdDateChanged(personId: Long): List<ManagementTierWithEndDate>
}
