package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTier
import uk.gov.justice.digital.hmpps.integrations.delius.management.ManagementTierId

interface ManagementTierDevRepository : JpaRepository<ManagementTier, ManagementTierId> {
    fun findByIdPersonId(personId: Long): ManagementTier
}
