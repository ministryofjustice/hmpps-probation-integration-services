package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}