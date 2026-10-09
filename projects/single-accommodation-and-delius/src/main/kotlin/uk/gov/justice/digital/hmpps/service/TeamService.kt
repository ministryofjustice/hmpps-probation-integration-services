package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedModel.PageMetadata
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.TeamStaffMember
import uk.gov.justice.digital.hmpps.model.TeamStaffResponse
import uk.gov.justice.digital.hmpps.repository.StaffRepository

@Service
class TeamService(
    private val staffRepository: StaffRepository,
) {
    fun getStaffForTeams(teamCodes: Collection<String>, pageable: PageRequest): TeamStaffResponse {
        val requestedTeamCodes = teamCodes.map(String::trim).filter(String::isNotBlank).distinct()

        if (requestedTeamCodes.isEmpty()) {
            return TeamStaffResponse(
                staff = emptyList(),
                page = PageMetadata(pageable.pageSize.toLong(), pageable.pageNumber.toLong(), 0, 0)
            )
        }

        val staffPage = staffRepository.findByTeamCodeIn(requestedTeamCodes, pageable)
        return TeamStaffResponse(
            staff = staffPage.content.map { it.toTeamStaffMember() },
            page = PageMetadata(
                pageable.pageSize.toLong(),
                pageable.pageNumber.toLong(),
                staffPage.totalElements,
                staffPage.totalPages.toLong()
            )
        )
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
