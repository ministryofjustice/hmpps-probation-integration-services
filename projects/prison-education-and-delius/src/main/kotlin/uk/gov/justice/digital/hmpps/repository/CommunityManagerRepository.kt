package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.CommunityManager

interface CommunityManagerRepository : JpaRepository<CommunityManager, Long> {
    fun findByPersonId(personId: Long): CommunityManager
}
