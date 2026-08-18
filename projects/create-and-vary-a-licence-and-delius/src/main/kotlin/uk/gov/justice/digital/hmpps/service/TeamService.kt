package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ManagedOffenderSummary
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.Caseload.CaseloadRole
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.CaseloadRepository

@Service
class TeamService(
    private val caseloadRepository: CaseloadRepository,
) {
    fun getManagedOffendersByTeam(teamCode: String): List<ManagedOffenderSummary> =
        caseloadRepository.findByTeamCodeAndRoleCodeOrderByAllocationDateDesc(
            teamCode,
            CaseloadRole.OFFENDER_MANAGER.value
        ).map {
            it.asManagedOffenderSummary()
        }
}
