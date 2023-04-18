package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.checkUserAccess
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.UserStaffRepository

@RestController
@Tag(name = "Users")
@PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISIONS_API')")
class UserController(
    private val userAccessRepository: UserAccessRepository,
    private val userStaffRepository: UserStaffRepository
) {
    @GetMapping("/user/{username}/access/{crn}")
    @Operation(
        summary = "Details of any restrictions or exclusions",
        description = "<p>Returns either an `exclusionMessage` or `restrictionMessage` if the user is not allowed to access the case. " +
            "<p>This can happen if the user is *excluded* from viewing the case (e.g. a family member), or if the case has been *restricted* to a subset of users that the user is not a part of (e.g. high profile cases)."
    )
    @ApiResponse(responseCode = "404", description = "A case with the provided CRN does not exist in Delius. Note: this could be the result of a merge or a deletion.")
    fun checkUserAccess(@PathVariable username: String, @PathVariable crn: String) = userAccessRepository.checkUserAccess(username, crn)

    @GetMapping("/user/{username}/staff")
    @Operation(summary = "Get the staff code for a user, if it exists")
    fun getStaffCode(@PathVariable username: String) = Staff(userStaffRepository.findUserStaffCode(username))
}
