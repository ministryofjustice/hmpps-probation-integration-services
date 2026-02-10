package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.aspect.WithDeliusUser
import uk.gov.justice.digital.hmpps.service.ContactLogService

@RestController
@Tag(name = "Contact")
@RequestMapping("/contact")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class ContactController(
    private val contactLogService: ContactLogService
) {

    @PostMapping("/{crn}")
    @WithDeliusUser
    @ResponseStatus(HttpStatus.CREATED)
    fun createContact(
        @PathVariable crn: String,
        @Valid @RequestBody createContact: CreateContact
    ) = contactLogService.createContact(crn, createContact)

    @GetMapping("/types")
    fun getContactTypes() = contactLogService.getContactTypes()
}