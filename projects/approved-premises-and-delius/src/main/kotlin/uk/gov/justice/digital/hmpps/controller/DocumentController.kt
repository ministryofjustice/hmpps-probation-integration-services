package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.APDocument
import uk.gov.justice.digital.hmpps.service.DocumentService

@RestController
@Tag(name = "Documents")
@RequestMapping("/documents/{crn}")
@PreAuthorize("hasRole('PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
class DocumentController(private val documentService: DocumentService) {

    @GetMapping(value = ["/{id}"])
    @Operation(summary = "Download document content")
    fun downloadDocument(
        @PathVariable crn: String,
        @PathVariable id: String
    ) = documentService.downloadDocument(crn, id)

    @PreAuthorize("hasRole('PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
    @Operation(
        summary = "List of documents held in Delius for the probation case",
        description = """List of documents available in Delius for the probation
            case identified by the CRN provided in the request. Document list
            includes summary information on the type and purpose of document held.
        """
    )
    @GetMapping(value = ["/all"])
    fun findDocuments(@PathVariable crn: String): List<APDocument> =
        documentService.getDocumentsByCrn(crn)
}
