package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.model.ProvidersResponse
import uk.gov.justice.digital.hmpps.model.SupervisorsResponse
import uk.gov.justice.digital.hmpps.model.TeamsResponse
import java.time.LocalDate

@Service
class ProvidersService(
    private val teamRepository: TeamRepository,
    private val probationAreaUserRepository: ProbationAreaUserRepository,
    private val userRepository: UserRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository
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

    fun getSupervisorsForTeam(teamCode: String): SupervisorsResponse {
        val staff = teamRepository.findStaffByTeamCode(teamCode)

        return SupervisorsResponse(supervisors = staff.map { it.toSupervisor() })
    }

    fun getSessions(teamCode: String, startDate: LocalDate, endDate: LocalDate): List<UnpaidWorkSessionDto> {
        val team = teamRepository.findTeamByCode(teamCode)
        val sessions = unpaidWorkAppointmentRepository.getUnpaidWorkSessionDetails(team.id, startDate, endDate)

        return sessions.map { it.toDto() }
    }
}