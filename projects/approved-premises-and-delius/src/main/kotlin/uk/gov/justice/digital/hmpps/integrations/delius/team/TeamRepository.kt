package uk.gov.justice.digital.hmpps.integrations.delius.team

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCodeAndProbationAreaCode(
        teamCode: String,
        probationAreaCode: String,
    ): Team?

    fun findByApprovedPremisesCodeCode(approvedPremisesCode: String): Team?
}

fun TeamRepository.getUnallocatedTeam(probationAreaCode: String) =
    findByCodeAndProbationAreaCode("${probationAreaCode}UAT", probationAreaCode)
        ?: throw NotFoundException("Unallocated Team", "provider", probationAreaCode)

fun TeamRepository.getApprovedPremisesTeam(approvedPremisesCode: String) =
    findByApprovedPremisesCodeCode(approvedPremisesCode)
        ?: throw NotFoundException("Team", "approved premises code", approvedPremisesCode)
