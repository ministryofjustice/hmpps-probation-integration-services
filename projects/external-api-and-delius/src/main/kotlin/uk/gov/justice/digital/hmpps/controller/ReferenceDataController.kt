package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ReferenceDataService

@RestController
@RequestMapping("/reference-data")
@PreAuthorize("hasRole('PROBATION_API__HMPPS_API__CASE_DETAIL')")
@Tag(name = "Identifier Converter", description = "Requires PROBATION_API__HMPPS_API__CASE_DETAIL")
class ReferenceDataController(
    private val referenceDataService: ReferenceDataService
) {
    @GetMapping
    @Operation(summary = "Gets delius reference data ")
    fun refData() = referenceDataService.getReferenceData()
}
