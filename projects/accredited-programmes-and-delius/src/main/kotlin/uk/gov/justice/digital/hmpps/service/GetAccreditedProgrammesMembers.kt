package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.AccreditedProgrammesMembersResponse
import uk.gov.justice.digital.hmpps.model.AccreditedProgrammesMembersResponse.Team
import uk.gov.justice.digital.hmpps.model.AccreditedProgrammesMembersResponse.Team.Member
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.repository.AccreditedProgrammesTeamMember
import uk.gov.justice.digital.hmpps.repository.StaffRepository

@Service
class GetAccreditedProgrammesMembers(
    private val staffRepository: StaffRepository,
) {
    companion object {
        private val LAU_CODES = mapOf(
            "N07" to "N07ACPR",
            "N50" to "N50APR",
            "N56" to "N56APCR",
            "N55" to "N55ACPR",
            "N58" to "N58ACPR",
            "N57" to "N57ACPR",
            "N03" to "N03ACPR",
            "N59" to "N59ACPR",
            "N54" to "N54ACPR",
            "N52" to "N52DDM",
            "N51" to "N51APR",
            "N53" to "N53ACPR",
        )
    }

    fun members(providerCode: String): AccreditedProgrammesMembersResponse {
        val lauCode = LAU_CODES[providerCode]
            ?: throw NotFoundException("No Accredited Programmes Local Admin Unit mapping found for region $providerCode")
        val teamMembers = staffRepository.findAccreditedProgrammesMembers(providerCode, lauCode)
        return teamMembers.toResponse()
    }

    fun List<AccreditedProgrammesTeamMember>.toResponse() =
        AccreditedProgrammesMembersResponse(teams = groupBy { it.teamCode to it.teamDescription }.map { (team, members) ->
            val (teamCode, teamDescription) = team
            Team(
                code = teamCode, description = teamDescription, members = members.map {
                    Member(
                        code = it.staffCode, name = Name(
                            forename = it.forename, middleNames = it.middleNames, surname = it.surname
                        )
                    )
                })
        })
}



