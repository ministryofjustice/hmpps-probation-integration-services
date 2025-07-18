package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.ProviderResponse
import uk.gov.justice.digital.hmpps.repository.ProbationAreaUserRepository
import uk.gov.justice.digital.hmpps.service.UserAccessService

@RestController
@PreAuthorize("hasRole('PROBATION_API__REMINDERS__USER_DETAILS')")
class UserController(
    private val probationAreaUserRepository: ProbationAreaUserRepository,
    private val userAccessService: UserAccessService
) {
    @GetMapping("/users/{username}/providers")
    fun getUserProviders(@PathVariable username: String) = ProviderResponse(
        providers = probationAreaUserRepository.findByUsername(username)
            .map { Provider(it.id.provider.code, it.id.provider.description) },
    )

    @PostMapping("/users/{username}/access")
    fun checkUserAccess(
        @PathVariable username: String,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns")
        @RequestBody crns: List<String>
    ) = userAccessService.userAccessFor(username, crns)
}
