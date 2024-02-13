package uk.gov.justice.digital.hmpps.api

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.DocumentService

@RestController
@PreAuthorize("hasRole('PROBATION_API__PRISONER_PROFILE__CASE_DETAIL')")
class DocumentResource(private val documentService: DocumentService) {

    @GetMapping(value = ["/probation-cases/{nomisId}/documents"])
    @Operation(
        summary = "List documents for a case",
        description = """Returns basic personal information for the case, along with a list of person-level documents 
            and event-level documents. Documents are annotated with the type and description, based on what they relate 
            to in the probation case (e.g. a court appearance, an event, etc).
        """
    )
    fun getDocuments(@PathVariable nomisId: String) = documentService.getDocumentsForCase(nomisId)

    @GetMapping(value = ["/document/{id}"])
    @Operation(summary = "Download document content")
    fun downloadDocument(@PathVariable id: String) = documentService.downloadDocument(id)
}
