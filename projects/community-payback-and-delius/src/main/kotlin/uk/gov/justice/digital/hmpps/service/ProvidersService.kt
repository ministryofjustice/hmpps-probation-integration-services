package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.model.ProvidersResponse
import uk.gov.justice.digital.hmpps.model.TeamsResponse

@Service
class ProvidersService(
    private val teamRepository: TeamRepository,
    private val probationAreaUserRepository: ProbationAreaUserRepository,
    private val userRepository: UserRepository
) {
    fun getProvidersForUser(username: String): ProvidersResponse {
        userRepository.findUserByUsername(username)
            ?: throw NotFoundException("User", "username", username)

        val probationAreaUsers = probationAreaUserRepository.findByUsername(username)

        return ProvidersResponse(providers = probationAreaUsers.map { it.toProviderCodeDescription() })
    }

    fun getUnpaidWorkTeams(providerCode: String): TeamsResponse {
        val teams = teamRepository.findUnpaidWorkTeamsByProviderCode(providerCode)

        return TeamsResponse(teams = teams.map { it.toCodeDescription() })
    }
}