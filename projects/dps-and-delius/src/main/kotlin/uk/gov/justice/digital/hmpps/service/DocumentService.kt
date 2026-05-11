package uk.gov.justice.digital.hmpps.service

import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.client.AlfrescoClient
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.*
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
    private val limitedAccessService: UserAccessService,
) {
    fun downloadDocument(id: String, username: String): ResponseEntity<StreamingResponseBody> {
        val document =
            documentRepository.findByAlfrescoId(id) ?: throw NotFoundException("Document", "alfrescoId", id)
        val person = personRepository.findById(document.personId)
            .orElseThrow { NotFoundException("Person", "id", document.personId) }
        checkForLao(person.crn, username)
        return alfrescoClient.streamDocument(id, document.name)
    }

    @Transactional
    fun getDocumentsForCase(nomisId: String, username: String) =
        personRepository.findByNomisId(nomisId)?.let { person ->
            checkForLao(person.crn, username)
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
    private fun checkForLao(crn: String, username: String) {
        val limitedAccessInfo = limitedAccessService.checkLimitedAccessFor(listOf(crn))
        limitedAccessInfo.access.firstOrNull { it.crn == crn }?.let { limitedAccess ->
            if (limitedAccess.userExcluded || limitedAccess.userRestricted) {
                throw AccessDeniedException("Access Denied for user $username to case with crn ${crn}")
            }
        }
    }
}
