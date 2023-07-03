package uk.gov.justice.digital.hmpps.integrations.delius.team

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCodeAndProbationAreaId(code: String, probationAreaId: Long): Team?
}

fun TeamRepository.getByCodeAndProbationAreaId(code: String, probationAreaId: Long) =
    findByCodeAndProbationAreaId(code, probationAreaId) ?: throw NotFoundException("Team", "code", code)
