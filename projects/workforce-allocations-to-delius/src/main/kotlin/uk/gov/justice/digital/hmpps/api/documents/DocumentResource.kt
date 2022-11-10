package uk.gov.justice.digital.hmpps.api.documents

import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
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
    @GetMapping
    fun findDocuments(@PathVariable crn: String): List<PersonDocument> =
        service.getDocumentsByCrn(crn)

    @PreAuthorize("hasRole('ROLE_WORKFORCE_DOCUMENT')")
    @GetMapping(value = ["/{id}"])
    fun getDocument(@PathVariable crn: String, @PathVariable id: String): ResponseEntity<Resource> =
        service.getDocument(crn, id)
}
