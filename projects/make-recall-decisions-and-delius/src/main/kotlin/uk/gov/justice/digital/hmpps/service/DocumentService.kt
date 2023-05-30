package uk.gov.justice.digital.hmpps.service

import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.HttpHeaders.CONTENT_LENGTH
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpHeaders.ETAG
import org.springframework.http.HttpHeaders.LAST_MODIFIED
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.alfresco.AlfrescoClient
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import kotlin.text.Charsets.UTF_8

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val alfrescoClient: AlfrescoClient
) {
    fun downloadDocument(crn: String, id: String): ResponseEntity<Resource> {
        val filename = documentRepository.findNameByPersonCrnAndAlfrescoId(crn, id)
            ?: throw NotFoundException("Document with id of $id not found for CRN $crn")

        val response = alfrescoClient.getDocument(id)

        return when {
            response.statusCode.is2xxSuccessful -> ResponseEntity.ok()
                .headers { it.putAll(response.sanitisedHeaders()) }
                .header(CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, UTF_8).build().toString())
                .body(response.body)

            response.statusCode.is4xxClientError -> throw NotFoundException("Document content with id of $id not found for CRN $crn")

            else -> throw RuntimeException("Failed to download document. Alfresco responded with ${response.statusCode}.")
        }
    }

    private fun <T> ResponseEntity<T>.sanitisedHeaders() = headers.filterKeys {
        it in listOf(
            CONTENT_LENGTH,
            CONTENT_TYPE,
            ETAG,
            LAST_MODIFIED
        )
    }
}
