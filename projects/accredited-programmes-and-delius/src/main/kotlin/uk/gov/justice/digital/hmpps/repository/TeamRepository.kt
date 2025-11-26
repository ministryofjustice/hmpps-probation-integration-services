package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.service.reportMissing

interface TeamRepository : JpaRepository<Team, Long> {
    @EntityGraph(attributePaths = ["provider", "localAdminUnit.probationDeliveryUnit", "officeLocations.localAdminUnit.probationDeliveryUnit"])
    fun findByCode(code: String): Team?

    @EntityGraph(attributePaths = ["provider", "localAdminUnit.probationDeliveryUnit", "officeLocations.localAdminUnit.probationDeliveryUnit"])
    fun findAllByCodeIn(code: Set<String>): List<Team>

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

fun TeamRepository.getAllByCodeIn(codes: List<String>) =
    codes.toSet().let { codes -> findAllByCodeIn(codes).associateBy { it.code }.reportMissing(codes) }
