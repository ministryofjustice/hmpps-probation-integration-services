package uk.gov.justice.digital.hmpps.controller.casedetails

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseRepository
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.getCase
import uk.gov.justice.digital.hmpps.controller.casedetails.model.CaseDetails

@Service
class CaseDetailsService(val caseRepository: CaseRepository, val caseMapper: CaseMapper) {
    fun getCaseDetails(crn: String, eventId: Long): CaseDetails {
        val case = caseRepository.getCase(crn, eventId)
        return caseMapper.withLanguage(case)
    }
}
