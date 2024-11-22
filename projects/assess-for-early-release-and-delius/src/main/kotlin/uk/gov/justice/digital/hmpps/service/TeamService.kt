package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ManagedOffender
import uk.gov.justice.digital.hmpps.entity.Caseload.CaseloadRole
import uk.gov.justice.digital.hmpps.entity.CaseloadRepository

@Service
class TeamService(
    private val caseloadRepository: CaseloadRepository,
) {
    fun getManagedOffendersByTeam(teamCode: String): List<ManagedOffender> =
        caseloadRepository.findByTeamCodeAndRoleCodeOrderByAllocationDateDesc(
            teamCode,
            CaseloadRole.OFFENDER_MANAGER.value
        ).map {
            it.asManagedOffender()
        }
}
