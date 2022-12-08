package uk.gov.justice.digital.hmpps.integrations.delius.document

import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocPerson
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.entityNotFound
import java.nio.charset.StandardCharsets

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
                RelatedTo(RelatedType.PRECONS, "Pre Cons"),
                null,
                person.preconDocCreatedDate,
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
                        "CPS Pack",
                        it.toDocumentEvent()
                    ),
                    null,
                    it.cpsCreatedDate,
                    false
                )
            }
    }

    fun getDocument(crn: String, id: String): ResponseEntity<Resource> {
        val person = docPersonRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val documentMetaData = documentRepository.findByAlfrescoIdAndSoftDeletedIsFalse(id) ?: throw NotFoundException(
            "Document",
            "id",
            id
        )
        if (person.id != documentMetaData.personId && docPersonRepository.findByCrnAndPreconDocId(
                crn,
                id
            ) == null && docEventRepository.findByPersonIdAndCpsDocumentId(person.id, id) == null
        ) {
            throw ConflictException("Document and CRN do not match")
        }
        val resource = alfrescoClient.getDocument(id)
        val headers = resource.headers.copyHeaders(
            HttpHeaders.ACCEPT_RANGES,
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ETAG,
            HttpHeaders.LAST_MODIFIED
        )
        headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(documentMetaData.name, StandardCharsets.UTF_8).build().toString()
        )
        return ResponseEntity(resource.body, headers, resource.statusCode)
    }

    private fun HttpHeaders.copyHeaders(vararg headerKeys: String): HttpHeaders {
        val newHeaders = HttpHeaders()
        for (key in headerKeys) {
            newHeaders[key] = getFirst(key)
        }
        return newHeaders
    }
}
