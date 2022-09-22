package uk.gov.justice.digital.hmpps.integrations.delius.presentencereport

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository

@Service
class PreSentenceReportService(
    private val courtReportRepository: CourtReportRepository
) {
    fun getPreSentenceReportContext(reportId: String): PreSentenceReportContext {
        val mapper = jacksonObjectMapper()
        val json = courtReportRepository.getCourtReportContextJson(reportId) ?: throw NotFoundException("CourtReport", "id", reportId)
        return mapper.readValue(json, PreSentenceReportContext::class.java)
    }
}
