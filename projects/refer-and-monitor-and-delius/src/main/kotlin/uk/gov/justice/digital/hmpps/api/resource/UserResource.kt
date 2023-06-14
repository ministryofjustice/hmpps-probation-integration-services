package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ManagedCases
import uk.gov.justice.digital.hmpps.service.ManagerService

@RestController
@RequestMapping("users/{username}")
class UserResource(private val managerService: ManagerService) {
    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("managed-cases")
    fun managedCases(@PathVariable username: String): ManagedCases =
        managerService.findCasesManagedBy(username)
}
