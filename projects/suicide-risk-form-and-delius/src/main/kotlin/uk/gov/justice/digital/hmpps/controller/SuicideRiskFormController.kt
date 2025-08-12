package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.model.InformationPageResponse
import uk.gov.justice.digital.hmpps.service.DetailsService
import uk.gov.justice.digital.hmpps.service.RegistrationsService

@RestController
class SuicideRiskFormController(
    private val detailsService: DetailsService,
    private val registrationsService: RegistrationsService
) {

    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @GetMapping(value = ["/basic-details/{crn}"])
    fun getBasicDetails(@PathVariable crn: String): BasicDetails = detailsService.basicDetails(crn)

    @PreAuthorize("hasRole('PROBATION_API__SUICIDE_RISK_FORM__CASE_DETAIL')")
    @GetMapping(value = ["/information-page/{crn}"])
    fun getInformationPage(@PathVariable crn: String): InformationPageResponse = registrationsService.informationPage(crn)
}
