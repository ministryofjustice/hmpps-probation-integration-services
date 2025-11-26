package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.service.ContactDetailsService

@RestController
class ContactDetailsController(val contactDetailsService: ContactDetailsService) {
    @PreAuthorize("hasRole('PROBATION_API__ESUPERVISION__CASE_DETAIL')")
    @GetMapping(value = ["/case/{crn}"])
    @Operation(summary = "Gets contact details for a person on probation, for a case by CRN")
    fun getContactDetails(@PathVariable crn: String) =
        contactDetailsService.getContactDetailsForCrn(crn)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Contact details not found")

    @PreAuthorize("hasRole('PROBATION_API__ESUPERVISION__CASE_DETAIL')")
    @PostMapping(value = ["/cases"])
    @Operation(summary = "Gets contact details for people on probation, for multiple cases by CRN")
    fun getContactDetailsForCases(@RequestBody crns: List<String>) =
        contactDetailsService.getContactDetailsForCrns(crns)
}