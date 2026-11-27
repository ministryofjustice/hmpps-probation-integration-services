package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ContactService

@RestController
@RequestMapping( "contacts")
class ContactController (
    private val contactService: ContactService
) {
    @PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__CASE_ADMIN')")
    @Operation(summary ="Hard delete contact by contactId")
    @DeleteMapping("/delete/{id}")
    fun deleteContactById(@PathVariable id: Long) = contactService.hardDeleteContact(id)
}