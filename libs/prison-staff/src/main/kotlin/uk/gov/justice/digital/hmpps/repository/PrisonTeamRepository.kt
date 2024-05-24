package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.PrisonTeam

interface PrisonTeamRepository : JpaRepository<PrisonTeam, Long> {
    fun findByCode(code: String): PrisonTeam?
}
