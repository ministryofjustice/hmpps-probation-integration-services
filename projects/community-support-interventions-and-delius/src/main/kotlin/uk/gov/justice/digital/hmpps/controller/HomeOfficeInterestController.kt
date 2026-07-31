package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.controller.model.HomeOfficeInterest
import uk.gov.justice.digital.hmpps.service.RegistrationService

@RestController
class HomeOfficeInterestController(
    val registrationService: RegistrationService
) {
    @PreAuthorize("hasRole('PROBATION_API__COMMUNITY_SUPPORT_INTERVENTIONS__CASE_DETAIL')")
    @GetMapping(value = ["/case/{crn}/home-office-interest"])
    fun getHomeOfficeInterest(
        @PathVariable("crn") crn: String
    ): HomeOfficeInterest = registrationService.getHomeOfficeInterest(crn)
}
