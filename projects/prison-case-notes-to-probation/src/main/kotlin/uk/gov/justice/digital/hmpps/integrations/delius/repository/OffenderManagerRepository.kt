package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OffenderManager

interface OffenderManagerRepository : JpaRepository<OffenderManager, Long> {
    fun findByOffenderCrn(crn: String): OffenderManager?
}