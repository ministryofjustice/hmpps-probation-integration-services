package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class ProvidersService(
    private val teamRepository: TeamRepository,
    private val probationAreaUserRepository: ProbationAreaUserRepository,
    private val userRepository: UserRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository
) {
    fun getProvidersForUser(username: String): ProvidersResponse {
        userRepository.findByUsername(username)
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

    fun getSupervisorsForUsername(username: String) = userRepository.getByUsername(username).staff?.let { staff ->
        SupervisorResponse(
            code = staff.code,
            isUnpaidWorkTeamMember = staff.teams.any { it.unpaidWorkTeam }
        )
    } ?: throw NotFoundException("Staff code for user", "username", username)

    fun getSessions(teamCode: String, startDate: LocalDate, endDate: LocalDate): SessionsResponse {
        if (ChronoUnit.DAYS.between(startDate, endDate) > 7) {
            throw IllegalArgumentException("Date range cannot be greater than 7 days")
        }

        val team = teamRepository.findTeamByCode(teamCode)
        val sessions = unpaidWorkAppointmentRepository.getUnpaidWorkSessionDetails(team.id, startDate, endDate)

        return SessionsResponse(sessions.map { it.toModel() })
    }
}