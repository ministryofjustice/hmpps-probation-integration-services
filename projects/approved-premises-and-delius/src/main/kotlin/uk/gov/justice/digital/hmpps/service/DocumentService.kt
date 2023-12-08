package uk.gov.justice.digital.hmpps.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.APDocument
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocPersonRepository

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val docPersonRepository: DocPersonRepository,
    private val alfrescoClient: AlfrescoClient,
) {
    fun downloadDocument(
        crn: String,
        id: String,
    ): ResponseEntity<StreamingResponseBody> {
        val filename =
            documentRepository.findNameByPersonCrnAndAlfrescoId(crn, id)
                ?: throw NotFoundException("Document with id of $id not found for CRN $crn")
        return alfrescoClient.streamDocument(id, filename)
    }

    fun getDocumentsByCrn(crn: String): List<APDocument> {
        val person = docPersonRepository.findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        val documents = ArrayList<APDocument>()
        documents +=
            documentRepository.findAllByPersonIdAndSoftDeletedIsFalse(person.id)
                .map {
                    APDocument(
                        it.alfrescoId,
                        if (it.findRelatedTo().event == null) "Offender" else "Conviction",
                        it.findRelatedTo().event?.eventNumber,
                        it.name,
                        it.findRelatedTo().type.name,
                        it.findRelatedTo().description,
                        it.createdDate,
                        it.lastSaved,
                        it.type.name,
                    )
                }

        return documents
    }
}
