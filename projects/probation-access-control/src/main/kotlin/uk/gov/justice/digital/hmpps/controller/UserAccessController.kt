package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
class UserAccessController(
    private val userAccessService: UserAccessService
) {
    @PreAuthorize("hasRole('PROBATION_API__ACCESS_CONTROLS__READ')")
    @GetMapping(value = ["/user/{username}/access/{crn}"])
    fun checkUserAccessForCrn(@PathVariable username: String, @PathVariable crn: String) =
        userAccessService.caseAccessFor(username, crn)

    @PreAuthorize("hasRole('PROBATION_API__ACCESS_CONTROLS__READ')")
    @PostMapping("/user/{username}/access")
    fun checkUserAccess(
        @PathVariable username: String,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns")
        @RequestBody crns: List<String>
    ) = userAccessService.userAccessFor(username, crns)

    @PreAuthorize("hasRole('PROBATION_API__ACCESS_CONTROLS__READ')")
    @GetMapping("/case/{crn}/access")
    fun getAllExclusionsAndRestrictionsForCrn(
        @PathVariable crn: String,
    ) = userAccessService.allCaseAccessForCrn(crn)
}
