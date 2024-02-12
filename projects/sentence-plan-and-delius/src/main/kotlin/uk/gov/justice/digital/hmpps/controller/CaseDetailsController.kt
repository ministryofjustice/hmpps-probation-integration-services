package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonDetailsService

@RestController
class CaseDetailsController(
    private val personDetailsService: PersonDetailsService
) {
    @PreAuthorize("hasRole('PROBATION_API__SENTENCE_PLAN__CASE_DETAIL')")
    @GetMapping(value = ["case-details/{crn}"])
    fun getCaseDetails(
        @PathVariable("crn") crn: String
    ) = personDetailsService.getPersonalDetails(crn)

    @PreAuthorize("hasRole('PROBATION_API__SENTENCE_PLAN__CASE_DETAIL')")
    @GetMapping(value = ["case-details/{crn}/first-appointment-date"])
    fun getFirstAppointmentDate(
        @PathVariable("crn") crn: String
    ) = personDetailsService.getFirstAppointmentDate(crn)
}
