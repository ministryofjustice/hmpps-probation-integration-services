package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.entityNotFound

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val docPersonRepository: DocPersonRepository,
    private val alfrescoClient: AlfrescoClient
) {

    fun getDocumentsByCrn(crn: String): List<PersonDocument> {
        val person = docPersonRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val documents = ArrayList<PersonDocument>()
        documents += documentRepository.findAllByPersonIdAndSoftDeletedIsFalse(person.id)
            .map {
                PersonDocument(
                    it.alfrescoId,
                    it.name,
                    it.findRelatedTo(),
                    it.lastSaved,
                    it.createdDate,
                    it.sensitive
                )
            }

        return documents.filter { it.relatedTo.name != entityNotFound }
    }

    fun getDocument(crn: String, id: String): ResponseEntity<StreamingResponseBody> {
        val person = docPersonRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val documentMetaData = documentRepository.findByAlfrescoIdAndSoftDeletedIsFalse(id) ?: throw NotFoundException(
            "Document",
            "id",
            id
        )
        if (person.id != documentMetaData.personId) {
            throw ConflictException("Document and CRN do not match")
        }
        return alfrescoClient.streamDocument(id, documentMetaData.name)
    }
}
