package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.model.Team
import uk.gov.justice.digital.hmpps.model.Teams
import uk.gov.justice.digital.hmpps.repository.StaffTeamRepository

@Service
class TeamsService(private val staffTeamRepository: StaffTeamRepository) {
    fun getTeamsForUser(username: String): Teams {
        val results = staffTeamRepository.findTeamsWithHierarchyByUsername(username)
        return Teams(teams = results.map { staffTeam ->
            val team = staffTeam.team
            val pdu = team.localAdminUnit.probationDeliveryUnit
            val provider = pdu.provider

            Team(
                code = team.code,
                description = team.description,
                pdu = CodedValue(
                    code = pdu.code,
                    description = pdu.description
                ),
                region = CodedValue(
                    code = provider.code,
                    description = provider.description
                )
            )
        })
    }
}