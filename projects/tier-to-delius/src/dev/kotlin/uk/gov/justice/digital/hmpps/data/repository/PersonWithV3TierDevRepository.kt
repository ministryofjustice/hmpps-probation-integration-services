package uk.gov.justice.digital.hmpps.data.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonWithV3Tier

interface PersonWithV3TierDevRepository : JpaRepository<PersonWithV3Tier, Long> {
    fun findByCrn(crn: String): PersonWithV3Tier?
}
