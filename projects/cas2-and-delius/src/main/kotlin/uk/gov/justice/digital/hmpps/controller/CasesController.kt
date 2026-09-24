package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.client.approvedpremises.model.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.service.RegistrationService

@RestController
class CasesController(private val registrationService: RegistrationService) {
    @PreAuthorize("hasRole('PROBATION_API__CAS_2__CASE_DETAIL')")
    @GetMapping(value = ["/cases/{crn}/sexual-offence-registrations"])
    fun getSexualOffenceRegistrations(@PathVariable crn: String): SexualOffenceRegistrations =
        registrationService.getSexualOffenceRegistrations(crn)
}
