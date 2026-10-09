package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedModel
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.TeamStaffMember
import uk.gov.justice.digital.hmpps.repository.StaffRepository
import uk.gov.justice.digital.hmpps.repository.TeamRepository

@Service
class TeamService(
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
) {
    fun getStaffForTeams(teamCodes: Collection<String>, pageable: PageRequest): PagedModel<TeamStaffMember> {
        val staffPage = staffRepository.findByTeamCodeIn(teamCodes.distinct(), pageable)
        return PagedModel(staffPage.map { it.toTeamStaffMember() })
    }
}

private fun Staff.toTeamStaffMember() = TeamStaffMember(
    username = user?.username,
    code = code,
    name = Name(
        forename = forename,
        middleName = middleName,
        surname = surname,
    )
)
