package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedModel.PageMetadata
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.TeamStaffMember
import uk.gov.justice.digital.hmpps.model.TeamStaffResponse
import uk.gov.justice.digital.hmpps.repository.StaffRepository
import uk.gov.justice.digital.hmpps.repository.TeamRepository

@Service
class TeamService(
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
) {
    fun getStaffForTeams(teamCodes: Collection<String>, pageable: PageRequest): TeamStaffResponse {
        val trimmedTeamCodes = teamCodes.map(String::trim)
        if (trimmedTeamCodes.isEmpty()) throw InvalidRequestException("At least one team code must be supplied")
        if (trimmedTeamCodes.any(String::isBlank)) throw InvalidRequestException("teamCodes must not contain blank values")

        val requestedTeamCodes = trimmedTeamCodes.distinct()
        val existingTeamCodes = teamRepository.findAllByCodeIn(requestedTeamCodes).map { it.code }.toSet()
        val missingTeamCodes = requestedTeamCodes.filterNot(existingTeamCodes::contains)
        if (missingTeamCodes.isNotEmpty()) {
            throw NotFoundException(
                if (missingTeamCodes.size == 1) {
                    "Team with code of ${missingTeamCodes.single()} not found"
                } else {
                    "Teams with codes of ${missingTeamCodes.joinToString(", ")} not found"
                }
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
