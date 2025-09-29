package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.ContactLogged
import uk.gov.justice.digital.hmpps.model.ContactsLogged
import uk.gov.justice.digital.hmpps.service.ContactService

@RestController
@RequestMapping("/case/{crn}/contacts")
@PreAuthorize("hasRole('PROBATION_API__HMPPS_API__CASE_DETAIL')")
@Tag(name = "Contacts", description = "Requires PROBATION_API__HMPPS_API__CASE_DETAIL")
class ContactsController(private val contactService: ContactService) {
    @GetMapping("/{contactId}")
    fun getContact(@PathVariable crn: String, @PathVariable contactId: Long): ContactLogged =
        contactService.getById(crn, contactId)

    @GetMapping
    fun getMappaContacts(
        @PathVariable crn: String,
        @RequestParam mappaCategories: List<Int>,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
    ): ContactsLogged = contactService.getMappaContacts(crn, mappaCategories, PageRequest.of(page,size))
}