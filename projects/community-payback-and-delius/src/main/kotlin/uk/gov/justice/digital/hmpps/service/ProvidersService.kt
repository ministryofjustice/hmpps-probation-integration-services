package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.staff.*
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProjectRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class ProvidersService(
    private val teamRepository: TeamRepository,
    private val probationAreaUserRepository: ProbationAreaUserRepository,
    private val userRepository: UserRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository
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
            isUnpaidWorkTeamMember = staff.teams.any { it.unpaidWorkTeam },
            unpaidWorkTeams = staff.toSupervisorTeams(),
        )
    } ?: throw NotFoundException("Staff code for user", "username", username)

    fun getProjectsForTeam(
        teamCode: String,
        typeCodes: List<String>,
        pageable: Pageable
    ): PagedModel<ProjectOutcomeStats> {
        val stats = unpaidWorkAppointmentRepository.getOutcomeStats(teamCode, typeCodes, pageable)
        val projects = unpaidWorkProjectRepository.findAllByIdIn(stats.content.map { (id) -> id }).associateBy { it.id }
        return stats.map { (id, overdueCount, overdueDays) ->
            ProjectOutcomeStats(Project(projects.getValue(id)), overdueCount, overdueDays)
        }.let { PagedModel(it) }
    }

    fun getSessions(teamCode: String, startDate: LocalDate, endDate: LocalDate): SessionsResponse {
        require(ChronoUnit.DAYS.between(startDate, endDate) <= 7) { "Date range cannot be greater than 7 days" }

        val team = teamRepository.findTeamByCode(teamCode)
        val sessions = unpaidWorkAppointmentRepository.getUnpaidWorkSessionDetails(team.id, startDate, endDate)

        return SessionsResponse(sessions.map { it.toModel() })
    }
}
