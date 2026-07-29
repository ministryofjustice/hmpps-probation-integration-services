package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository

interface PersonWithV3TierRepository : JpaRepository<PersonWithV3Tier, Long> {
    fun findByCrnAndSoftDeletedFalse(crn: String): PersonWithV3Tier?
}