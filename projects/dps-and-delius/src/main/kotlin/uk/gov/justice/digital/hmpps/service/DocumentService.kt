package uk.gov.justice.digital.hmpps.service

import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.AlfrescoClient
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.entity.Disposal
import uk.gov.justice.digital.hmpps.entity.DocumentRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.entity.relatesToEvent
import uk.gov.justice.digital.hmpps.entity.typeDescription
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Conviction
import uk.gov.justice.digital.hmpps.model.Document
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.ProbationDocumentsResponse
import kotlin.text.Charsets.UTF_8

@Service
class DocumentService(
    private val personRepository: PersonRepository,
    private val documentRepository: DocumentRepository,
    private val alfrescoClient: AlfrescoClient
) {
    fun downloadDocument(id: String): ResponseEntity<Resource> {
        val filename = documentRepository.findNameByAlfrescoId(id) ?: throw NotFoundException("Document", "alfrescoId", id)
        val response = alfrescoClient.getDocument(id)
        return when {
            response.statusCode.is2xxSuccessful -> ResponseEntity.ok()
                .headers { it.putAll(response.sanitisedHeaders()) }
                .header(CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, UTF_8).build().toString())
                .body(response.body)

            response.statusCode.is4xxClientError -> throw NotFoundException("Document content", "alfrescoId", id)

            else -> throw RuntimeException("Failed to download document. Alfresco responded with ${response.statusCode}.")
        }
    }

    fun getDocumentsForCase(nomisId: String) = personRepository.findByNomisId(nomisId)?.let { person ->
        val documents = documentRepository.getPersonAndEventDocuments(person.id)
        val eventDocuments = documents.filter { it.relatesToEvent() }.groupBy { it.eventId }
        ProbationDocumentsResponse(
            crn = person.crn,
            name = Name(
                person.forename,
                listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                person.surname
            ),
            documents = documents.filter { !it.relatesToEvent() }.map { document ->
                Document(
                    id = document.alfrescoId,
                    name = document.name,
                    description = document.description,
                    type = document.typeDescription(),
                    author = document.author,
                    createdAt = document.createdAt?.atZone(EuropeLondon)
                )
            }.sortedByDescending { it.createdAt },
            convictions = person.events.map { event ->
                Conviction(
                    title = event.disposal?.description ?: event.courtAppearances.latestOutcome()?.description,
                    offence = event.mainOffence.offence.subCategoryDescription,
                    date = event.referralDate,
                    active = event.active,
                    institutionName = event.disposal?.custody?.institution?.name,
                    documents = eventDocuments[event.id]?.map { document ->
                        Document(
                            id = document.alfrescoId,
                            name = document.name,
                            description = document.description,
                            type = document.typeDescription(),
                            author = document.author,
                            createdAt = document.createdAt?.atZone(EuropeLondon)
                        )
                    }?.sortedByDescending { it.createdAt } ?: emptyList()
                )
            }.sortedByDescending { it.date }
        )
    } ?: throw NotFoundException("Person", "nomisId", nomisId)

    private val Disposal.description get() = "${type.description}${lengthString?.let { " ($it)" } ?: ""}"
    private val Disposal.lengthString get() = length?.let { "$length ${lengthUnits!!.description}" }
    private fun List<CourtAppearance>.latestOutcome() = filter { it.outcome != null }.maxByOrNull { it.date }?.outcome
    private fun <T> ResponseEntity<T>.sanitisedHeaders() = headers.filterKeys {
        it in listOf(
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ETAG,
            HttpHeaders.LAST_MODIFIED
        )
    }
}
