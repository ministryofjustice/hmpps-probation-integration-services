package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val personRepository: PersonRepository,
    private val alfrescoClient: AlfrescoClient
) {

    fun getDocumentsByCrn(crn: String): List<PersonDocument> {
        val person = personRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val documents = documentRepository.findAllByPersonIdAndSoftDeletedIsFalse(person.id)
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
        return documents.filter { it.relatedTo.name != ("ENTITY_NOT_FOUND") }
    }

    fun getDocument(crn: String, id: String): ResponseEntity<Resource> {
        val person = personRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val documentMetaData = documentRepository.findByAlfrescoIdAndSoftDeletedIsFalse(id)
        if (person.id != documentMetaData?.personId) {
            throw ConflictException("Document and CRN do not match")
        }
        return alfrescoClient.getDocument(id)
    }
}
