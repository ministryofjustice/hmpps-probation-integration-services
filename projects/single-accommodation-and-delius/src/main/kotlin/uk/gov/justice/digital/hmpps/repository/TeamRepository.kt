package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.staff.Team

interface TeamRepository : JpaRepository<Team, Long> {
    fun existsByCode(teamCode: String): Boolean
}