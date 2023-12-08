package uk.gov.justice.digital.hmpps.integrations.delius.presentencereport

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository

@Service
class PreSentenceReportService(
    auditedInteractionService: AuditedInteractionService,
    private val courtReportRepository: CourtReportRepository,
    private val objectMapper: ObjectMapper,
) : AuditableService(auditedInteractionService) {
    fun getPreSentenceReportContext(reportId: String): PreSentenceReportContext {
        val json =
            courtReportRepository.getCourtReportContextJson(reportId)
                ?: throw NotFoundException("CourtReport", "id", reportId)
        return objectMapper.readValue(json, PreSentenceReportContext::class.java)
    }
}
