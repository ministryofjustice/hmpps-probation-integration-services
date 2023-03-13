package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@RequestMapping("/user")
class UserResource(private val userAccessService: UserAccessService) {

    @PreAuthorize("hasAnyRole('ROLE_ALLOCATION_CONTEXT', 'ROLE_WORKFORCE_DOCUMENT')")
    @RequestMapping("/{username}/access-controls", method = [RequestMethod.GET, RequestMethod.POST])
    fun userAccessCheck(@PathVariable username: String, @RequestBody crns: List<String>) =
        userAccessService.userAccessFor(username, crns)
}
