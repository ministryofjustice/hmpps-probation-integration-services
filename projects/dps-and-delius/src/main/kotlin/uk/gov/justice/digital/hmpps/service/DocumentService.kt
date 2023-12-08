package uk.gov.justice.digital.hmpps.service

import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
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

@Service
class DocumentService(
    private val personRepository: PersonRepository,
    private val documentRepository: DocumentRepository,
    private val alfrescoClient: AlfrescoClient,
) {
    fun downloadDocument(id: String): ResponseEntity<StreamingResponseBody> {
        val filename =
            documentRepository.findNameByAlfrescoId(id) ?: throw NotFoundException("Document", "alfrescoId", id)
        return alfrescoClient.streamDocument(id, filename)
    }

    @Transactional
    fun getDocumentsForCase(nomisId: String) =
        personRepository.findByNomisId(nomisId)?.let { person ->
            val documents = documentRepository.getPersonAndEventDocuments(person.id)
            val eventDocuments = documents.filter { it.relatesToEvent() }.groupBy { it.eventId }
            ProbationDocumentsResponse(
                crn = person.crn,
                name =
                    Name(
                        person.forename,
                        listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                        person.surname,
                    ),
                documents =
                    documents.filter { !it.relatesToEvent() }.map { document ->
                        Document(
                            id = document.alfrescoId,
                            name = document.name,
                            description = document.description,
                            type = document.typeDescription(),
                            author = document.author,
                            createdAt = document.createdAt?.atZone(EuropeLondon),
                        )
                    }.sortedByDescending { it.createdAt },
                convictions =
                    person.events.map { event ->
                        Conviction(
                            title = event.disposal?.description ?: event.courtAppearances.latestOutcome()?.description,
                            offence = event.mainOffence.offence.subCategoryDescription,
                            date = event.referralDate,
                            active = event.active,
                            institutionName = event.disposal?.custody?.institution?.name,
                            documents =
                                eventDocuments[event.id]?.map { document ->
                                    Document(
                                        id = document.alfrescoId,
                                        name = document.name,
                                        description = document.description,
                                        type = document.typeDescription(),
                                        author = document.author,
                                        createdAt = document.createdAt?.atZone(EuropeLondon),
                                    )
                                }?.sortedByDescending { it.createdAt } ?: emptyList(),
                        )
                    }.sortedByDescending { it.date },
            )
        } ?: throw NotFoundException("Person", "nomisId", nomisId)

    private val Disposal.description get() = "${type.description}${lengthString?.let { " ($it)" } ?: ""}"
    private val Disposal.lengthString get() = length?.let { "$length ${lengthUnits!!.description}" }

    private fun List<CourtAppearance>.latestOutcome() = filter { it.outcome != null }.maxByOrNull { it.date }?.outcome
}
