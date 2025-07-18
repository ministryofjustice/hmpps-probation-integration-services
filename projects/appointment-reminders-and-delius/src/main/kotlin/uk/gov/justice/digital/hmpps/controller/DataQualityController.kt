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
import uk.gov.justice.digital.hmpps.repository.PersonRepository

@RestController
@PreAuthorize("hasRole('PROBATION_API__REMINDERS__CASE_DETAILS')")
class DataQualityController(
    private val personRepository: PersonRepository,
    private val ldapTemplate: LdapTemplate,
) {
    @GetMapping("/data-quality/{providerCode}/invalid-mobile-numbers/count")
    fun getInvalidMobileNumbersCount(
        @PathVariable providerCode: String
    ) = personRepository.getInvalidMobileNumberCount(providerCode)

    @GetMapping("/data-quality/{providerCode}/invalid-mobile-numbers")
    fun getInvalidMobileNumbers(
        @PathVariable providerCode: String,
        @PageableDefault pageable: Pageable
    ) = personRepository.getCasesWithInvalidMobileNumber(providerCode, pageable)
        .map { person -> person.toProbationCase { ldapTemplate.findEmailByUsername(it) } }
        .let { PagedModel(it) }

    @GetMapping("/data-quality/{providerCode}/missing-mobile-numbers")
    fun getMissingMobileNumbers(
        @PathVariable providerCode: String,
        @PageableDefault pageable: Pageable
    ) = personRepository.getCasesWithMissingMobileNumber(providerCode, pageable)
        .map { person -> person.toProbationCase { ldapTemplate.findEmailByUsername(it) } }
        .let { PagedModel(it) }

    @GetMapping("/data-quality/{providerCode}/duplicate-mobile-numbers")
    fun getDuplicateMobileNumbers(
        @PathVariable providerCode: String,
        @PageableDefault pageable: Pageable
    ) = personRepository.getCasesWithDuplicateMobileNumbers(providerCode, pageable)
        .map { person -> person.toProbationCase { ldapTemplate.findEmailByUsername(it) } }
        .let { PagedModel(it) }
}
