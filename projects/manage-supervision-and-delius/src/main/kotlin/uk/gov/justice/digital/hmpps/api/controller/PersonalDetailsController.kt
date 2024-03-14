package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonalDetailsService

@Validated
@RestController
@Tag(name = "Personal Details")
@RequestMapping("/personal-details/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class PersonalDetailsController(private val personalDetailsService: PersonalDetailsService) {

    @GetMapping
    @Operation(summary = "Personal Details containing address, personal contacts and documents ")
    fun getPersonalDetails(@PathVariable crn: String) = personalDetailsService.getPersonalDetails(crn)

    @GetMapping("/document/{documentId}")
    @Operation(summary = "Download document")
    fun downloadDocument(@PathVariable crn: String, @PathVariable documentId: String) =
        personalDetailsService.downloadDocument(crn, documentId)

    @GetMapping("/summary")
    @Operation(summary = "Person Summary")
    fun getPersonSummary(@PathVariable crn: String) =
        personalDetailsService.getPersonSummary(crn)

    @GetMapping("/personal-contact/{id}")
    @Operation(summary = "Person Contact")
    fun getPersonContact(@PathVariable crn: String, @PathVariable id: Long) =
        personalDetailsService.getPersonContact(crn, id)
}
