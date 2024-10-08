package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseDetailsService

@RestController
@PreAuthorize("hasRole('PROBATION_API__HMPPS_API__CASE_DETAIL')")
@Tag(name = "Identifier Converter", description = "Requires PROBATION_API__HMPPS_API__CASE_DETAIL")
class CaseIdentifierController(
    private val caseDetailsService: CaseDetailsService
) {
    @GetMapping(value = ["/identifier-converter/noms-to-crn/{nomsId}"])
    @Operation(summary = "Gets the corresponding CRN from delius for the provided nomsId if found")
    fun nomsToCrn(@PathVariable("nomsId") nomsId: String) = caseDetailsService.getCrnForNomsId(nomsId)

    @GetMapping(value = ["/exists-in-delius/crn/{crn}"])
    @Operation(summary = "Check if crn exists in delius for the provided crn")
    fun checkIfPersonExists(@PathVariable("crn") crn: String) = caseDetailsService.checkIfPersonExists(crn)
}
