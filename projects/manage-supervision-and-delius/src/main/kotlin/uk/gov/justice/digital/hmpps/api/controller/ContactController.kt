package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.api.model.contact.UpdateContact
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

    @PatchMapping("/{contactId}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateContact(
        auth: Authentication,
        @PathVariable contactId: Long,
        @RequestPart("files") file: MultipartFile?,
        @RequestPart("request", required = true) @Valid request: UpdateContact
    ) =  contactLogService.updateContactWithDocuments(auth.name, contactId,  file, request)

}