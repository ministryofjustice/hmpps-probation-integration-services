package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import uk.gov.justice.digital.hmpps.model.TeamCodeResponse

@Service
class TeamService(
    private val caseloadRepository: CaseloadRepository,
) {
    @Transactional
    fun getTeamsManagingCase(
        crn: String,
        staffCode: String?,
    ) = TeamCodeResponse(
        teamCodes = caseloadRepository.findTeamsManagingCase(crn, staffCode),
    )
}
