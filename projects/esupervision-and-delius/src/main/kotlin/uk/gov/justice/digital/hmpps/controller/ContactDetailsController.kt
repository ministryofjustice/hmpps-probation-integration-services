package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.model.PersonalDetails
import uk.gov.justice.digital.hmpps.model.UpdateContactDetails
import uk.gov.justice.digital.hmpps.service.ContactDetailsService
import uk.gov.justice.digital.hmpps.service.PersonalDetailsValidationService

@RestController
class ContactDetailsController(
    val contactDetailsService: ContactDetailsService,
    val personalDetailsValidationService: PersonalDetailsValidationService
) {
    @PreAuthorize("hasRole('PROBATION_API__ESUPERVISION__CASE_DETAIL')")
    @GetMapping(value = ["/case/{crn}"])
    @Operation(summary = "Gets contact details for a person on probation, for a case by CRN")
    fun getContactDetails(@PathVariable crn: String) =
        contactDetailsService.getContactDetailsForCrn(crn)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Contact details not found")

    @PreAuthorize("hasRole('PROBATION_API__ESUPERVISION__CASE_DETAIL')")
    @PostMapping(value = ["/cases"])
    @Operation(summary = "Gets contact details for people on probation, for multiple cases by CRN")
    fun getContactDetailsForCases(
        @RequestBody @Size(
            min = 1,
            max = 500,
            message = "Please provide between 1 and 500 crns"
        ) crns: List<String>
    ) =
        contactDetailsService.getContactDetailsForCrns(crns)

    @PreAuthorize("hasRole('PROBATION_API__ESUPERVISION__CASE_DETAIL')")
    @PostMapping(value = ["/case/{crn}/validate-details"])
    @Operation(summary = "Validates personal details for a person on probation")
    fun validatePersonalDetails(
        @PathVariable crn: String,
        @RequestBody personalDetails: PersonalDetails
    ) = personalDetailsValidationService.validatePersonalDetails(personalDetails)

    @PreAuthorize("hasRole('PROBATION_API__ESUPERVISION__CASE_DETAIL')")
    @PutMapping(value = ["/case/{crn}/contact-details"])
    @Operation(summary = "Updates contact details (mobile and email) for a person on probation")
    fun updateContactDetails(
        @PathVariable crn: String,
        @RequestBody update: UpdateContactDetails
    ) = contactDetailsService.updateContactDetails(crn, update)

    @PreAuthorize("hasRole('PROBATION_API__ESUPERVISION__CASE_DETAIL')")
    @GetMapping(value = ["/user/{username}/alerts"])
    @Operation(summary = "Gets the count of unreviewed check-in alerts for a user")
    fun getAlerts(@PathVariable username: String) =
        contactDetailsService.getAlertCount(username)
}