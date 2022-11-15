package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocPerson
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.entityNotFound

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val docPersonRepository: DocPersonRepository,
    private val docEventRepository: DocEventRepository,
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

        if (person.preconDocId != null) {
            documents += PersonDocument(
                person.preconDocId,
                person.preconDocName!!,
                RelatedTo(RelatedType.PRECONS),
                null,
                person.preconDocCreatedDate!!,
                false
            )
        }

        documents += cpsDocuments(person)

        return documents.filter { it.relatedTo.name != entityNotFound }
    }

    private fun cpsDocuments(person: DocPerson): List<PersonDocument> {
        return docEventRepository.findByPersonId(person.id)
            .filter { it.cpsDocumentId != null }
            .map {
                PersonDocument(
                    it.cpsDocumentId,
                    it.cpsDocumentName!!,
                    RelatedTo(
                        RelatedType.CPSPACK,
                        it.disposal?.type?.description ?: "",
                        it.toDocumentEvent()
                    ),
                    null,
                    it.cpsCreatedDate!!,
                    false
                )
            }
    }

    fun getDocument(crn: String, id: String): ResponseEntity<Resource> {
        val person = docPersonRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val documentMetaData = documentRepository.findByAlfrescoIdAndSoftDeletedIsFalse(id)
        if (person.id != documentMetaData?.personId) {
            throw ConflictException("Document and CRN do not match")
        }
        return alfrescoClient.getDocument(id)
    }
}
