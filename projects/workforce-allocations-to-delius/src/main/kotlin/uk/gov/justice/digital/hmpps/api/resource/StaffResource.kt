package uk.gov.justice.digital.hmpps.api.resource

import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.StaffService

@Validated
@RestController
@RequestMapping("/staff")
class StaffResource(private val service: StaffService) {

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = """Personal and and summary caseload information for
            the identified probation officer""",
        description = """Summary information on the probation caseload
            for the probation officer identified in the request. Used
            to support the 'Officer View' capability of the HMPPS
            Workload service which shows statistics on the staff members
            caseload
            """
    )
    @GetMapping("{code}/officer-view")
    fun officerView(
        @PathVariable code: String
    ) = service.getOfficerView(code)

    @PreAuthorize("hasRole('PROBATION_API__WORKFORCE_ALLOCATIONS__CASE_DETAIL')")
    @Operation(
        summary = """Personal information along with a list of active
            cases for the identified probation practitioner""",
        description = """Summary information on the full case list of
            of the probation practitioner identified in the request.
            Used to support the 'Case Details' capability of the HMPPS
            Workload service, which show details of the current case load
            of the staff member for capacity planning
            """

    )
    @PostMapping("{code}/active-cases")
    fun activeCases(
        @PathVariable code: String,
        @RequestBody crns: List<String>
    ) = service.getActiveCases(code, crns)
}
