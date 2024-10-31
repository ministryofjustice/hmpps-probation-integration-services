package uk.gov.justice.digital.hmpps.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.typeCode
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.typeDescription
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.model.DocumentResponse

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val personRepository: PersonRepository,
    private val alfrescoClient: AlfrescoClient
) {
    fun downloadDocument(crn: String, id: String): ResponseEntity<StreamingResponseBody> {
        val filename = documentRepository.findNameByPersonCrnAndAlfrescoId(crn, id)
            ?: throw NotFoundException("Document with id of $id not found for CRN $crn")
        return alfrescoClient.streamDocument(id, filename)
    }

    fun getDocumentsByCrn(crn: String): List<DocumentResponse> {
        val person = personRepository.getByCrn(crn)
        return documentRepository.getPersonAndEventDocuments(person.id).map {
            DocumentResponse(
                id = it.id,
                level = if (it.eventNumber == null) "Offender" else "Conviction",
                eventNumber = it.eventNumber,
                filename = it.name,
                typeCode = it.typeCode,
                typeDescription = it.typeDescription,
                dateSaved = it.dateSaved.atZone(EuropeLondon),
                dateCreated = (it.dateCreated ?: it.dateSaved).atZone(EuropeLondon),
                description = it.description,
            )
        }
    }
}
