package uk.gov.justice.digital.hmpps.integrations.delius.presentencereport

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.psr.PsrClient
import java.net.URI
import java.util.UUID

@Service
class PreSentenceReportService(
    auditedInteractionService: AuditedInteractionService,
    @Value("\${integrations.pre-sentence-reports.base-url}") private val psrBaseUrl: String,
    @Value("\${integrations.new-tech.base-url}") private val newTechBaseUrl: String,
    private val courtReportRepository: CourtReportRepository,
    private val objectMapper: ObjectMapper,
    private val psrClient: PsrClient,
    private val documentService: DocumentService
) : AuditableService(auditedInteractionService) {
    fun getPreSentenceReportContext(reportId: String): PreSentenceReportContext {
        val json = courtReportRepository.getCourtReportContextJson(reportId)
            ?: throw NotFoundException("CourtReport", "id", reportId)
        return objectMapper.readValue(json, PreSentenceReportContext::class.java)
    }

    fun createPreSentenceReport(crn: String, courtReportId: Long, reportType: String, username: String): PsrReference {
        val courtReport = courtReportRepository.findByIdOrNull(courtReportId)
            ?: throw NotFoundException("CourtReport", "id", courtReportId)
        val person = courtReport.person
        check(person.crn == crn) { "Mismatch between crn and court report id" }
        val psrRef = psrClient.createReport(
            URI.create("$psrBaseUrl/api/v1/report/$reportType"),
            mapOf("crn" to person.crn, "eventNumber" to courtReport.appearance.event.number)
        )
        val pdf = psrClient.getPsrReport(URI.create("$psrBaseUrl/api/v1/report/${psrRef.id}/pdf"))
        documentService.createNewCourtReportDocument(reportType, courtReport, psrRef, pdf, username)
        return psrRef
    }

    fun getPreSentenceReportUrl(uuid: UUID): String =
        documentService.getPreSentenceReportUrl(uuid, psrBaseUrl)

    fun getLegacyNewTechReportUrl(uuid: UUID, username: String): String =
        documentService.getLegacyNewTechReportUrl(uuid, newTechBaseUrl, username)
}

data class PsrReference(val id: UUID, val urn: String)
