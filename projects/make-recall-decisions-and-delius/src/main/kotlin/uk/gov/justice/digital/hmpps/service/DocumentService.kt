package uk.gov.justice.digital.hmpps.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.api.model.Document
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val alfrescoClient: AlfrescoClient
) {
    fun downloadDocument(crn: String, id: String): ResponseEntity<StreamingResponseBody> {
        val filename = documentRepository.findNameByPersonCrnAndAlfrescoId(crn, id)
            ?: throw NotFoundException("Document with id of $id not found for CRN $crn")
        return alfrescoClient.streamDocument(id, filename)
    }

    fun findApprovedPremisesDocuments(crn: String): List<Document> =
        documentRepository.getApprovedPremisesDocuments(crn).map {
            Document(
                id = it.alfrescoId,
                name = it.name,
                description = it.description,
                type = it.type,
                author = it.author,
                createdAt = it.createdAt?.atZone(EuropeLondon),
                lastUpdatedAt = it.lastUpdatedAt?.atZone(EuropeLondon),
            )
        }
}
