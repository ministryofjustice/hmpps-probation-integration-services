package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.TeamsResponse
import uk.gov.justice.digital.hmpps.api.model.toStaffMember
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository

@Service
class TeamService(
    private val staffRepository: StaffRepository,
    private val ldapService: LdapService,
) {
    fun getTeams(teamCodes: List<String>) = TeamsResponse(
        teamCodes.associateWith { teamCode ->
            val staff = staffRepository.findActiveStaffInTeam(teamCode)
            val emails = ldapService.findEmailsForStaffIn(staff)
            staff.map { it.toStaffMember(emails[it.user?.username]) }
        }
    )
}
