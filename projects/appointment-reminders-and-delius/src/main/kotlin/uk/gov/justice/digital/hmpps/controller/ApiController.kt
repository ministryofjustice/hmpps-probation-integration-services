package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.ProviderResponse
import uk.gov.justice.digital.hmpps.repository.ProbationAreaUserRepository

@RestController
class ApiController(
    private val probationAreaUserRepository: ProbationAreaUserRepository
) {
    @GetMapping("/users/{username}/providers")
    @PreAuthorize("hasRole('PROBATION_API__REMINDERS__USER_DETAILS')")
    fun getUserProviders(@PathVariable username: String) = ProviderResponse(
        providers = probationAreaUserRepository.findByUsername(username)
            .map { Provider(it.id.provider.code, it.id.provider.description) },
    )
}
