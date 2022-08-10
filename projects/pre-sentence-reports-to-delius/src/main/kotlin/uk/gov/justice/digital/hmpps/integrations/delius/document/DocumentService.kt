package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReportRepository
import uk.gov.justice.digital.hmpps.listener.HmppsEvent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class DocumentService(
    auditedInteractionService: AuditedInteractionService,
    private val documentRepository: DocumentRepository,
    private val courtReportRepository: CourtReportRepository,
    private val alfrescoClient: AlfrescoClient
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun updateCourtReportDocument(hmppsEvent: HmppsEvent, file: ByteArray) =
        audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
            val document = documentRepository.findByExternalReference(hmppsEvent.additionalInformation.reportId)
                ?: throw NotFoundException("Document", "externalReference", hmppsEvent.additionalInformation.reportId)
            it["documentId"] = document.id
            it["alfrescoDocumentId"] = document.alfrescoId
            val courtReport = courtReportRepository.findById(document.courtReportId).orElseThrow {
                NotFoundException("CourtReport", "id", document.courtReportId)
            }

            if(courtReport.person.crn!=hmppsEvent.personReference.findCrn()){
                throw ConflictException("Court report ${courtReport.id} not for ${hmppsEvent.personReference.findCrn()}")
            }

            alfrescoClient.releaseDocument(hmppsEvent.additionalInformation.reportId)
            alfrescoClient.updateDocument(
                hmppsEvent.additionalInformation.reportId,
                populateBodyValues(hmppsEvent, document.courtReportId, file).build()
            )

            document.documentName = hmppsEvent.filename()
        }


    private fun populateBodyValues(hmppsEvent: HmppsEvent, courtReportId: Long, file: ByteArray): MultipartBodyBuilder {
        val crn = hmppsEvent.personReference.findCrn()!!
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("CRN", crn, MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityId", courtReportId.toString(), MediaType.TEXT_PLAIN)
        bodyBuilder.part("author", "pre-sentence-reports-to-delius", MediaType.TEXT_PLAIN)
        bodyBuilder.part("filedata", file, MediaType.APPLICATION_OCTET_STREAM).filename(hmppsEvent.filename())
        bodyBuilder.part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        bodyBuilder.part("entityType", "COURT_REPORT", MediaType.TEXT_PLAIN)

        return bodyBuilder
    }

    fun HmppsEvent.filename() = URLEncoder.encode(
        "${personReference.findCrn()}_pre-sentence-report_${additionalInformation.reportId}.pdf",
        StandardCharsets.UTF_8.name()
    )
}
