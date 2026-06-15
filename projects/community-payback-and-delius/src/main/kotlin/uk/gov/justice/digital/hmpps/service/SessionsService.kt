package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.staff.TeamRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Session
import uk.gov.justice.digital.hmpps.utils.Extensions.reportMissing
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class SessionsService(
    private val teamRepository: TeamRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
) {
    fun getSessions(
        teamCodes: List<String>,
        startDate: LocalDate,
        endDate: LocalDate,
        typeCodes: List<String>,
        pageable: Pageable
    ): PagedModel<Session> {
        require(ChronoUnit.DAYS.between(startDate, endDate) <= 7) { "Date range cannot be greater than 7 days" }

        val teams = teamRepository.findTeamsByCodeIn(teamCodes)

        teams.associateBy { it.code }
            .reportMissing(teamCodes.toSet())

        val teamIds = teams.map { it.id }
        val sessions = unpaidWorkAppointmentRepository.getUnpaidWorkSessionDetails(
            teamIds,
            startDate,
            endDate,
            typeCodes,
            pageable
        )

        return PagedModel(
            sessions.map { it.toModel() }
        )
    }
}