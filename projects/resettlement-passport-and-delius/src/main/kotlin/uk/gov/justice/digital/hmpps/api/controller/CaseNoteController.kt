package uk.gov.justice.digital.hmpps.api.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.CreateContact
import uk.gov.justice.digital.hmpps.service.CaseNoteService

@RestController
@RequestMapping("/nomis-case-note")
class CaseNoteController(private val caseNoteService: CaseNoteService) {

    @PreAuthorize("hasRole('PROBATION_API__RESETTLEMENT_PASSPORT__APPOINTMENT_RW')")
    @PostMapping("/{crn}")
    @ResponseStatus(HttpStatus.CREATED)
    fun createContact(@PathVariable crn: String, @RequestBody @Valid createContact: CreateContact) {
        caseNoteService.createContact(crn, createContact)
    }
}

