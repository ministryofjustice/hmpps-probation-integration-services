package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.DocumentService

@RestController
@Tag(name = "Documents")
@RequestMapping("/document/{crn}")
@PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISIONS_API')")
class DocumentController(private val documentService: DocumentService) {
    @GetMapping(value = ["/{id}"])
    @Operation(summary = "Download document content")
    fun downloadDocument(
        @PathVariable crn: String,
        @PathVariable id: String,
    ) = documentService.downloadDocument(crn, id)
}
