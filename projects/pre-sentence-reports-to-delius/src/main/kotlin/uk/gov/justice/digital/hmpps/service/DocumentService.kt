package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.Document.Companion.psrUrn
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.psrId
import uk.gov.justice.digital.hmpps.messaging.username
import uk.gov.justice.digital.hmpps.user.AuditUserService
import java.time.ZonedDateTime

@Service
class DocumentService(
    auditedInteractionService: AuditedInteractionService,
    private val documentRepository: DocumentRepository,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    private val auditUserService: AuditUserService,
    private val entityManager: EntityManager,
) : AuditableService(auditedInteractionService)
{
    @Transactional
    fun uploadDocument(event: HmppsDomainEvent, file: ByteArray) = audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
        check(file.isPdf()) { "Invalid PDF file: ${event.detailUrl}" }
        val document = getDocument(event, it)
        document.name = document.name.plus(".pdf")
        document.status = "Y"
        document.workInProgress = "N"
        document.lastSaved = ZonedDateTime.now()
        document.createdDatetime = ZonedDateTime.now()
        document.lastUpdatedUserId = auditUserService.findUser(event.username)?.id
            ?: throw NotFoundException("User", "username", event.username)
        document.alfrescoId = alfrescoUploadClient.upload(document.toMultipart(file)).id
        documentRepository.save(document)
        updateCourtReport(document.courtReport.id)
    }

    fun updateCourtReport(courtReportId: Long) {
        val query = entityManager.createNativeQuery("update court_report set completed_date = :completedDate where court_report_id = :psrId")
        query.setParameter("completedDate", ZonedDateTime.now())
        query.setParameter("psrId", courtReportId)
        query.executeUpdate()
    }

    private fun getDocument(event: HmppsDomainEvent, audit: AuditedInteraction.Parameters): Document {
        val urn = psrUrn(event.psrId)
        return documentRepository.findByExternalReference(urn)?.also {
            audit["documentId"] = it.id
            audit["alfrescoDocumentId"] = it.alfrescoId
            audit["entityId"] = it.courtReport.id
            audit["tableName"] = it.tableName
            audit["externalReference"] = urn
        } ?: throw NotFoundException("Document", "externalReference", urn)
    }

    private fun uk.gov.justice.digital.hmpps.entity.Document.toMultipart(file: ByteArray) = MultipartBodyBuilder().apply {
        part("CRN", person.crn, MediaType.TEXT_PLAIN)
        part("fileName", name, MediaType.TEXT_PLAIN)
        part("filedata", file, MediaType.APPLICATION_OCTET_STREAM).filename(name)
        part("author", "Service,Pre Sentence Report", MediaType.TEXT_PLAIN)
        part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        //entityType in Alfresco does not always correspond exactly to tableName in Delius.
        // See https://github.com/ministryofjustice/delius/blob/0087df0cb1dd5305fb44f89f4bf78dfc6b3916f6/NDelius-lib/src/main/java/uk/co/bconline/ndelius/util/iwp/MetadataMapper.java#L18-L39
        part("entityType", "COURTREPORT", MediaType.TEXT_PLAIN)
        part("entityId", courtReport.id.toString(), MediaType.TEXT_PLAIN)
        part("locked", "true", MediaType.TEXT_PLAIN)
    }.build()

    private fun ByteArray.isPdf() = take(4).toByteArray().contentEquals("%PDF".toByteArray())
}