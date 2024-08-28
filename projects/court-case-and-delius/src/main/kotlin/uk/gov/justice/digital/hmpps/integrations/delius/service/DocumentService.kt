package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getPerson
import java.time.Instant
import java.time.LocalDateTime

@Service
class DocumentService(
    private val personRepository: PersonRepository,
    private val documentRepository: DocumentRepository,
    private val alfrescoClient: AlfrescoClient
) {

    fun getDocumentsGroupedFor(crn: String, filterData: DocumentFilter): OffenderDocuments {
        val person = personRepository.getPerson(crn)

        if (filterData.subtype != null && filterData.type == null) {
            throw InvalidRequestException("subtype of ${filterData.subtype} was supplied but no type. subtype can only be supplied when a valid type is supplied")
        }
        val typeFilter = filterData.type?.toEnumOrElseThrow<DocumentType>("type of ${filterData.type} was not valid")
        val subType = filterData.subtype?.toEnumOrElseThrow<SubType>("subtype of ${filterData.subtype} was not valid")
        subType?.let {
            if (!typeFilter!!.subtypes.contains(it)) {
                throw InvalidRequestException("subtype of ${filterData.subtype} was not valid for type ${typeFilter.name}")
            }
        }

        val allDocuments = documentRepository.getPersonAndEventDocuments(person.id)
        val (convictionDocuments, offenderDocuments) = allDocuments.partition { it.relatesToEvent() }
        val documents = offenderDocuments.map { it.toOffenderDocumentDetail() }
            .filter { filter(it, filterData) }
        val convictions = convictionDocuments
            .groupBy { it.eventId }
            .map {
                ConvictionDocuments(it.key.toString(),
                    it.value.map { d -> d.toOffenderDocumentDetail() }
                        .filter { odd -> filter(odd, filterData) })
            }

        return OffenderDocuments(documents, convictions)
    }

    fun downloadDocument(crn: String, id: String): ResponseEntity<StreamingResponseBody> {
        val person = personRepository.getPerson(crn)
        val filename = documentRepository.findNameByPersonIdAndAlfrescoId(person.id, id)
            ?: throw NotFoundException("Document with id of $id not found for CRN $crn")
        return alfrescoClient.streamDocument(id, filename)
    }

    private fun filter(record: OffenderDocumentDetail, filter: DocumentFilter): Boolean {
        var include = true
        if (filter.type != null) {
            (record.type.code == filter.type).also { include = it }
        }
        if (filter.subtype != null) {
            (record.subType?.code == filter.subtype).also { include = it }
        }
        return include
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrElseThrow(message: String) =
    T::class.java.enumConstants.firstOrNull { it.name == this } ?: throw InvalidRequestException(message)

private fun Instant?.toLocalDateTime() = this?.let { LocalDateTime.ofInstant(it, EuropeLondon) }

fun Document.toOffenderDocumentDetail() = OffenderDocumentDetail(
    id = alfrescoId,
    documentName = name,
    author = author,
    type = KeyValue(typeCode(), typeDescription()),
    extendedDescription = description,
    lastModifiedAt = lastModifiedAt.toLocalDateTime(),
    createdAt = createdAt.toLocalDateTime() ?: lastModifiedAt.toLocalDateTime(),
    parentPrimaryKeyId = primaryKeyId,
    subType = subTypeDescription?.let { KeyValue(code = subTypeCode, description = it) },
    reportDocumentDates = ReportDocumentDates(
        requestedDate = dateRequested?.let { LocalDateTime.ofInstant(it, EuropeLondon).toLocalDate() },
        requiredDate = dateRequired?.let { LocalDateTime.ofInstant(it, EuropeLondon).toLocalDate() },
        completedDate = completedDate?.let { LocalDateTime.ofInstant(it, EuropeLondon) },
    ).takeUnless { it.requestedDate == null && it.requiredDate == null && completedDate == null }
)
