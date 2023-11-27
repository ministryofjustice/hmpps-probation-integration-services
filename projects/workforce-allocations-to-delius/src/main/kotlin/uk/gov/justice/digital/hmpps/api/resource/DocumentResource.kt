package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentService
import uk.gov.justice.digital.hmpps.integrations.delius.document.PersonDocument

@RestController
@RequestMapping("/offenders/{crn}/documents")
class DocumentResource(private val service: DocumentService) {

    @PreAuthorize("hasRole('ROLE_WORKFORCE_DOCUMENT')")
    @Operation(
        summary = "List of documents held in Delius for the probation case",
        description = """List of documents available in Delius for the probation
            case identified by the CRN provided in the request. Document list
            includes summary information on the type and purpose of document held.
            Used to support the 'Document List' view of the HMPPS Workforce service
            which is used to give detailed information on the case when allocating
            to a probation practitioner
        """
    )
    @GetMapping
    fun findDocuments(@PathVariable crn: String): List<PersonDocument> =
        service.getDocumentsByCrn(crn)

    @PreAuthorize("hasRole('ROLE_WORKFORCE_DOCUMENT')")
    @Operation(
        summary = "Fetch a complete document from Delius/Alfresco",
        description = """Returns the full document identified by the CRN and
            document id provided in the request. Document is returned in the
            format stored in Alfresco. Used to support downloading documents
            from the document list in the HMPPS Workload service
        """
    )
    @GetMapping(value = ["/{id}"])
    fun getDocument(@PathVariable crn: String, @PathVariable id: String) = service.getDocument(crn, id)
}
