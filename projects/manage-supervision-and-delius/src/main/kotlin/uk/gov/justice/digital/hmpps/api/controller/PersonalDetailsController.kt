package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonalContactEditRequest
import uk.gov.justice.digital.hmpps.service.PersonalDetailsService

@Validated
@RestController
@Tag(name = "Personal Details")
@RequestMapping("/personal-details/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class PersonalDetailsController(private val personalDetailsService: PersonalDetailsService) {

    @PostMapping
    @Operation(summary = "Update personal details")
    fun updatePersonalDetails(@PathVariable crn: String, @Valid @RequestBody request: PersonalContactEditRequest) =
        personalDetailsService.updatePersonalDetails(crn, request)

    @GetMapping
    @Operation(summary = "Personal Details containing address, personal contacts and documents ")
    fun getPersonalDetails(@PathVariable crn: String) = personalDetailsService.getPersonalDetails(crn)

    @GetMapping("/main-address/note/{noteId}")
    @Operation(summary = "Person Contact")
    fun getMainAddressSingleNote(@PathVariable crn: String, @PathVariable noteId: Int) =
        personalDetailsService.getMainAddressSingleNote(crn, noteId)

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

    @GetMapping("/personal-contact/{id}/note/{noteId}")
    @Operation(summary = "Person Contact")
    fun getPersonContactSingleNote(@PathVariable crn: String, @PathVariable id: Long, @PathVariable noteId: Int) =
        personalDetailsService.getPersonContactSingleNote(crn, id, noteId)

    @GetMapping("/addresses")
    @Operation(summary = "Person Addresses")
    fun getPersonAddresses(@PathVariable crn: String) =
        personalDetailsService.getPersonAddresses(crn)

    @GetMapping("/addresses/{id}/note/{noteId}")
    @Operation(summary = "Person Addresses")
    fun getPersonAddressesSingleNote(@PathVariable crn: String, @PathVariable id: Long, @PathVariable noteId: Int) =
        personalDetailsService.getPersonAddressSingleNote(crn, id, noteId)

    @GetMapping("/circumstances")
    @Operation(summary = "Person Circumstances")
    fun getPersonCircumstances(@PathVariable crn: String) =
        personalDetailsService.getPersonCircumstances(crn)

    @GetMapping("/circumstances/{circumstanceId}/note/{noteId}")
    @Operation(summary = "Person Circumstances")
    fun getPersonCircumstancesSingleNote(
        @PathVariable crn: String,
        @PathVariable circumstanceId: Long,
        @PathVariable noteId: Int
    ) =
        personalDetailsService.getPersonCircumstancesSingleNote(crn, circumstanceId, noteId)

    @GetMapping("/disabilities")
    @Operation(summary = "Person Disabilities")
    fun getPersonDisabilities(@PathVariable crn: String) =
        personalDetailsService.getPersonDisabilities(crn)

    @GetMapping("/disability/{disabilityId}/note/{noteId}")
    @Operation(summary = "Person Disabilities")
    fun getPersonDisabilitiesSingleNote(
        @PathVariable crn: String,
        @PathVariable disabilityId: Int,
        @PathVariable noteId: Int
    ) =
        personalDetailsService.getPersonDisabilitySingleNote(crn, disabilityId, noteId)

    @GetMapping("/provisions")
    @Operation(summary = "Person Provisions")
    fun getPersonProvisions(@PathVariable crn: String) =
        personalDetailsService.getPersonProvisions(crn)

    @GetMapping("/provisions/{provisionId}/note/{noteId}")
    @Operation(summary = "Person Provisions")
    fun getPersonProvisionsSingleNote(
        @PathVariable crn: String,
        @PathVariable provisionId: Long,
        @PathVariable noteId: Int
    ) =
        personalDetailsService.getPersonProvisionsSingleNote(crn, provisionId, noteId)
}
