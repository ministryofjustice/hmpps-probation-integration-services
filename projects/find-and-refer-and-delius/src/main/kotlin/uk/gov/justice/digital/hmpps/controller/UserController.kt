package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonService
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@Tag(name = "User Access")
@RequestMapping("users/{username}")
class UserController(
    private val userService: UserAccessService,
    private val personService: PersonService
) {
    @PreAuthorize("hasRole('PROBATION_API__FIND_AND_REFER__CASE_DETAIL')")
    @GetMapping("/access/{identifier}")
    @Operation(summary = "Provides the case access for a given user and case identifier (crn or prisoner number)")
    fun checkAccess(@PathVariable username: String, @PathVariable identifier: String) =
        userService.caseAccessFor(username, personService.resolveCrn(identifier))
}
