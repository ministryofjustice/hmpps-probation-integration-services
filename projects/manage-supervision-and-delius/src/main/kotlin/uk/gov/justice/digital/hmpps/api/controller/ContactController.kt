package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.aspect.WithDeliusUser
import uk.gov.justice.digital.hmpps.service.ContactLogService
import uk.gov.justice.digital.hmpps.service.UserService

@RestController
@Tag(name = "Contact")
@RequestMapping("/contact")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class ContactController(
    private val contactLogService: ContactLogService,
    private val userService: UserService
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

    @GetMapping("/{username}/enforcements")
    fun getEnforcementContacts(
        @PathVariable username: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "false") filterDueDate: Boolean
    ) = userService.getEnforcementContacts(
        username,
        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "contact_date")),
        filterDueDate
    )
}