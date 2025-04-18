package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentDetails
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentSearch
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonDocuments
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getSummary
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentEntity
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.DocumentsRepository
import java.time.LocalTime

@Service
class DocumentsService(
    private val documentsRepository: DocumentsRepository,
    private val personRepository: PersonRepository,
) {

    fun getDocuments(crn: String, pageable: Pageable, sortedBy: String): PersonDocuments {
        val summary = personRepository.getSummary(crn)
        val documents = documentsRepository.findByOffenderId(summary.id, pageable)
        return PersonDocuments(
            personSummary = summary.toPersonSummary(),
            totalElements = documents.totalElements.toInt(),
            totalPages = documents.totalPages,
            documents = documents.content.map { it.toDocumentDetails() },
            sortedBy = sortedBy
        )
    }

    fun search(documentSearch: DocumentSearch, crn: String, pageable: Pageable, sortedBy: String): PersonDocuments {
        val summary = personRepository.getSummary(crn)
        val documents = documentsRepository.search(
            summary.id,
            documentSearch.name,
            documentSearch.dateFrom?.toLocalDate()?.atStartOfDay(),
            documentSearch.dateTo?.toLocalDate()?.atTime(LocalTime.MAX),
            pageable
        )
        return PersonDocuments(
            personSummary = summary.toPersonSummary(),
            totalElements = documents.totalElements.toInt(),
            totalPages = documents.totalPages,
            documents = documents.content.map { it.toDocumentDetails() },
            sortedBy = sortedBy
        )
    }
}

fun DocumentEntity.toDocumentDetails() = DocumentDetails(
    alfrescoId,
    offenderId,
    name,
    level,
    type,
    createdAt,
    lastUpdatedAt,
    author,
    status,
    workInProgress ?: false
)
