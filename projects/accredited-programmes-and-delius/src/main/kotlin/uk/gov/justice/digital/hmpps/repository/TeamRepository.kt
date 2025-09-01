package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}

fun TeamRepository.getByCode(code: String): Team =
    findByCode(code) ?: throw NotFoundException("Team", "code", code)