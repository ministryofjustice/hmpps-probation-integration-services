package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.EntityManager
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.client.AlfrescoUploadClient
import uk.gov.justice.digital.hmpps.client.RestClientUtils.nullIfNotFound
import uk.gov.justice.digital.hmpps.entity.Document
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import java.time.ZonedDateTime
import java.util.*
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService

@Service
@Transactional
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val alfrescoUploadClient: AlfrescoUploadClient,
    auditedInteractionService: AuditedInteractionService,
    private val entityManager: EntityManager,
) : AuditableService(auditedInteractionService) {



    fun deleteDocument(document: Document) = audit(BusinessInteractionCode.DELETE_DOCUMENT) {
        populateAudit(document, it)

        nullIfNotFound { alfrescoUploadClient.release(document.alfrescoId) }
        nullIfNotFound { alfrescoUploadClient.delete(document.alfrescoId) }

        documentRepository.delete(document)
        val hasDocuments = documentRepository.existsByTableNameAndPrimaryKeyIdAndIdNotAndSoftDeletedFalse(
            document.tableName,
            document.primaryKeyId,
            document.id
        )
        updateContactDocumentLinked(document.primaryKeyId, hasDocuments)
    }


    private fun updateContactDocumentLinked(contactId: Long, hasDocuments: Boolean) {
        entityManager.createNativeQuery(
            "update contact set document_linked = :documentLinked where contact_id = :contactId"
        )
            .setParameter("documentLinked", if (hasDocuments) "Y" else "N")
            .setParameter("contactId", contactId)
            .executeUpdate()
    }

    private fun populateAudit(document: Document, audit: AuditedInteraction.Parameters) {
        audit["documentId"] = document.id
        audit["alfrescoDocumentId"] = document.alfrescoId
        audit["entityId"] = document.primaryKeyId
        audit["tableName"] = document.tableName
        audit["externalReference"] = document.externalReference
    }


}

