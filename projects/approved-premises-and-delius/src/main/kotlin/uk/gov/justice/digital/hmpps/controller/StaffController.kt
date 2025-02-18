package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.StaffService

@RestController
@PreAuthorize("hasRole('PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
class StaffController(
    private val staffService: StaffService
) {
    @Operation(
        summary = "List all members of staff that are keyworkers in the Approved Premises",
        description = """An Approved Premises is defined in Delius as part of reference data.
            Probation staff members can be linked to an Approved Premises as either staff
            members or keyworkers via the "Approved Premises and Teams" screen of the Delius
            user interface. Keyworkers are defined via the "Approved Premises and Key Workers"
            screen of the Delius user interface. Respond with a list of all members of staff
            defined as keyworkers for the Approved Premises identified by the requested code.
            """
    )
    @GetMapping(value = ["/approved-premises/{code}/staff"])
    fun getStaff(
        @PathVariable code: String,
        @RequestParam(required = false, defaultValue = "false") keyWorker: Boolean,
        @PageableDefault(value = 100) pageable: Pageable = Pageable.ofSize(100)
    ) = staffService.getStaffInApprovedPremises(code, keyWorker, pageable)

    @Operation(summary = "Get the staff details by username")
    @GetMapping(value = ["/staff/{userName}"])
    fun getStaffByUsername(
        @PathVariable userName: String
    ) = staffService.getStaffByUsername(userName)

    @Operation(summary = "Get the staff details by code")
    @GetMapping(value = ["/staff"], params = ["code"])
    fun getStaffByCode(
        @RequestParam code: String
    ) = staffService.getStaffByCode(code)
}
