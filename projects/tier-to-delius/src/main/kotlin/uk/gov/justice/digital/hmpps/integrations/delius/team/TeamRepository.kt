package uk.gov.justice.digital.hmpps.integrations.delius.team

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}

fun TeamRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Team", "code", code)
