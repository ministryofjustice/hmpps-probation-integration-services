package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.model.CodedValue
import uk.gov.justice.digital.hmpps.model.UserTeam
import uk.gov.justice.digital.hmpps.repository.TeamRepository

@Service
class TeamService(private val teamRepository: TeamRepository) {
    fun findUserTeams(username: String): List<UserTeam> =
        teamRepository.findUserTeams(username).map { it.asUserTeam() }
}

private fun Team.asUserTeam() = UserTeam(code, description, pdu(), area())
private fun Team.pdu() = with(localAdminUnit.probationDeliveryUnit) {
    CodedValue(code, description)
}

private fun Team.area() = CodedValue(provider.code, provider.description)