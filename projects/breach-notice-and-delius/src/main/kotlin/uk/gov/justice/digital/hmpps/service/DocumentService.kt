package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.EntityManager
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.Document
import uk.gov.justice.digital.hmpps.integrations.delius.Document.Companion.breachNoticeUrn
import uk.gov.justice.digital.hmpps.integrations.delius.DocumentRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.breachNoticeId
import uk.gov.justice.digital.hmpps.messaging.username
import uk.gov.justice.digital.hmpps.user.AuditUserService
import java.time.ZonedDateTime
import java.util.*

@Service
class DocumentService(
    auditedInteractionService: AuditedInteractionService,
    private val auditUserService: AuditUserService,
    private val documentRepository: DocumentRepository,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    private val entityManager: EntityManager,
) : AuditableService(auditedInteractionService) {
    fun uploadDocument(event: HmppsDomainEvent, file: ByteArray) = audit(BusinessInteractionCode.UPLOAD_DOCUMENT) {
        check(file.isPdf()) { "Invalid PDF file: ${event.detailUrl}" }

        val document = getDocument(event, it)
        document.name = document.name.replace(Regex("\\.docx?$"), ".pdf")
        document.status = "Y"
        document.workInProgress = "N"
        document.lastSaved = ZonedDateTime.now()
        document.lastUpdatedUserId = auditUserService.findUser(event.username)?.id
            ?: throw NotFoundException("User", "username", event.username)

        alfrescoUploadClient.delete(document.alfrescoId)
        document.alfrescoId = alfrescoUploadClient.upload(document.toMultipart(file)).id

        documentRepository.save(document)
    }

    @Transactional
    fun deleteDocument(event: HmppsDomainEvent) = audit(BusinessInteractionCode.DELETE_DOCUMENT) {
        val document = getDocument(event, it)
        documentRepository.delete(document)
        updateParent(document)
        alfrescoUploadClient.release(document.alfrescoId)
        alfrescoUploadClient.delete(document.alfrescoId)
    }

    private fun getDocument(event: HmppsDomainEvent, audit: AuditedInteraction.Parameters): Document {
        val urn = breachNoticeUrn(UUID.fromString(event.breachNoticeId))
        return documentRepository.findByExternalReference(urn)?.also {
            audit["documentId"] = it.id
            audit["alfrescoDocumentId"] = it.alfrescoId
            audit["entityId"] = it.primaryKeyId
            audit["tableName"] = it.tableName
            audit["externalReference"] = urn
        } ?: throw NotFoundException("Document", "externalReference", urn)
    }

    private fun updateParent(document: Document) {
        val hasOtherDocuments = documentRepository
            .existsByTableNameAndPrimaryKeyIdAndIdNot(document.tableName, document.primaryKeyId, document.id)

        // update deploy/database/access.yml if new tables are included
        val query = when (document.tableName) {
            "ADDRESSASSESSMENT" -> entityManager.createNativeQuery("update address_assessment set document_linked = :documentLinked where address_assessment_id = :primaryKeyId")
            "ASSESSMENT" -> entityManager.createNativeQuery("update assessment set document_linked = :documentLinked where assessment_id = :primaryKeyId")
            "CONTACT" -> entityManager.createNativeQuery("update contact set document_linked = :documentLinked where contact_id = :primaryKeyId")
            "NSI" -> entityManager.createNativeQuery("update nsi set document_linked = :documentLinked where nsi_id = :primaryKeyId")
            "REFERRAL" -> entityManager.createNativeQuery("update referral set document_linked = :documentLinked where referral_id = :primaryKeyId")
            "REGISTRATION" -> entityManager.createNativeQuery("update registration set document_linked = :documentLinked where registration_id = :primaryKeyId")
            "UPW_APPOINTMENT" -> entityManager.createNativeQuery("update upw_appointment set document_linked = :documentLinked where upw_appointment_id = :primaryKeyId")
            else -> return
        }

        query
            .setParameter("documentLinked", if (hasOtherDocuments) "Y" else "N")
            .setParameter("primaryKeyId", document.primaryKeyId)
            .executeUpdate()
    }

    private fun Document.toMultipart(file: ByteArray) = MultipartBodyBuilder().apply {
        part("CRN", person.crn, MediaType.TEXT_PLAIN)
        part("fileName", name, MediaType.TEXT_PLAIN)
        part("filedata", file, MediaType.APPLICATION_OCTET_STREAM).filename(name)
        part("author", "Service,Breach Notice", MediaType.TEXT_PLAIN)
        part("docType", "DOCUMENT", MediaType.TEXT_PLAIN)
        //entityType in Alfresco does not always correspond exactly to tableName in Delius.
        // See https://github.com/ministryofjustice/delius/blob/0087df0cb1dd5305fb44f89f4bf78dfc6b3916f6/NDelius-lib/src/main/java/uk/co/bconline/ndelius/util/iwp/MetadataMapper.java#L18-L39
        part("entityType", populateEntityType(tableName), MediaType.TEXT_PLAIN)
        part("entityId", primaryKeyId.toString(), MediaType.TEXT_PLAIN)
        part("locked", "true", MediaType.TEXT_PLAIN)
    }.build()

    private fun ByteArray.isPdf() = take(4).toByteArray().contentEquals("%PDF".toByteArray())
}

fun populateEntityType(entityType: String): String {
    return when (entityType) {
        "APPROVED_PREMISES_REFERRAL" -> "APREFERRAL"
        "COURT_REPORT" -> "COURTREPORT"
        "INSTITUTIONAL_REPORT" -> "INSTITUTIONALREPORT"
        "NSI" -> "PROCESSCONTACT"
        "PERSONAL_CIRCUMSTANCE" -> "PERSONALCIRCUMSTANCE"
        "UPW_APPOINTMENT" -> "UPWAPPOINTMENT"
        else -> entityType
    }
}
