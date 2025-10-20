package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.toCodeDescription
import uk.gov.justice.digital.hmpps.model.TeamsResponse

@Service
class TeamService(
    private val teamRepository: TeamRepository
) {
    fun getUnpaidWorkTeams(providerCode: String): TeamsResponse {
        val teams = teamRepository.findUnpaidWorkTeamsByProviderCode(providerCode)

        return TeamsResponse(teams = teams.map { it.toCodeDescription() })
    }
}