package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?

    @Query(
        """
            select t from Staff s
            join s.teams t
            where upper(s.user.username) = upper(:username) and t.endDate is null
        """
    )
    fun findUserTeams(username: String): List<Team>
}

fun TeamRepository.getByCode(code: String): Team =
    findByCode(code) ?: throw NotFoundException("Team", "code", code)