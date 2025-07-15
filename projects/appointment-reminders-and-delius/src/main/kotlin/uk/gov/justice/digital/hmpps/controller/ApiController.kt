package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.ldap.core.LdapTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.model.ProviderResponse
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.ProbationAreaUserRepository

@RestController
class ApiController(
    private val probationAreaUserRepository: ProbationAreaUserRepository,
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate,
) {
    @GetMapping("/users/{username}/providers")
    @PreAuthorize("hasRole('PROBATION_API__REMINDERS__USER_DETAILS')")
    fun getUserProviders(@PathVariable username: String) = ProviderResponse(
        providers = probationAreaUserRepository.findByUsername(username)
            .map { Provider(it.id.provider.code, it.id.provider.description) },
    )

    @GetMapping("/data-quality/{providerCode}/stats")
    @PreAuthorize("hasRole('PROBATION_API__REMINDERS__CASE_DETAILS')")
    fun getDataQualityStats(
        @PathVariable providerCode: String
    ) = personRepository.getDataQualityStats(providerCode)

    @GetMapping("/data-quality/{providerCode}/invalid-mobile-numbers")
    @PreAuthorize("hasRole('PROBATION_API__REMINDERS__CASE_DETAILS')")
    fun getInvalidMobileNumbers(
        @PathVariable providerCode: String,
        @PageableDefault pageable: Pageable
    ) = personRepository.getCasesWithInvalidMobileNumber(providerCode, pageable)
        .map { person -> person.toProbationCase { ldapTemplate.findEmailByUsername(it) } }
        .let { PagedModel(it) }

    @GetMapping("/data-quality/{providerCode}/missing-mobile-numbers")
    @PreAuthorize("hasRole('PROBATION_API__REMINDERS__CASE_DETAILS')")
    fun getMissingMobileNumbers(
        @PathVariable providerCode: String,
        @PageableDefault pageable: Pageable
    ) = personRepository.getCasesWithMissingMobileNumber(providerCode, pageable)
        .map { person -> person.toProbationCase { ldapTemplate.findEmailByUsername(it) } }
        .let { PagedModel(it) }
}
