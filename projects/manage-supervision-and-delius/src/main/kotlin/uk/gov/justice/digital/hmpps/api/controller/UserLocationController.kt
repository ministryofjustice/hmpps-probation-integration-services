package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserLocationService

@RestController
@Tag(name = "Locations")
@RequestMapping("/user/{username}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class UserLocationController(private val userLocationService: UserLocationService) {

    @GetMapping("/locations")
    @Operation(summary = "Display user locations")
    fun getUserOfficeLocations(@PathVariable username: String) = userLocationService.getUserOfficeLocations(username)
}