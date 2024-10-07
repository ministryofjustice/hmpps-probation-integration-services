package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.DocumentFilter
import uk.gov.justice.digital.hmpps.integrations.delius.service.DocumentService

@RestController
@RequestMapping("probation-case/{crn}/documents")
@PreAuthorize("hasRole('PROBATION_API__COURT_CASE__CASE_DETAIL')")
class DocumentResource(private val documentService: DocumentService) {

    @GetMapping("/grouped")
    fun getOffenderDocumentsGrouped(
        @PathVariable crn: String,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) subType: String?,
    ) = documentService.getDocumentsGroupedFor(crn, DocumentFilter(type, subType))

    @GetMapping("/{documentId}")
    fun getOffenderDocumentById(
        @PathVariable crn: String,
        @PathVariable documentId: String
    ) = documentService.downloadDocument(crn, documentId)
}
