package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CaseDetailService

@RestController
@Tag(name = "Cases")
@PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__CASE_DETAIL')")
class CaseController(private val caseDetailService: CaseDetailService) {
    @GetMapping(value = ["/case/{crn}/personal-details"])
    fun getPersonalDetails(@PathVariable crn: String) =
        caseDetailService.getPersonalDetails(crn)

    @GetMapping(value = ["/case/{crn}/sentence/{eventNumber}"])
    fun getSentence(@PathVariable crn: String, @PathVariable eventNumber: String) =
        caseDetailService.getSentence(crn, eventNumber)

    @GetMapping(value = ["/case/{crn}/sentence/{eventNumber}/offences"])
    fun getOffences(@PathVariable crn: String, @PathVariable eventNumber: String) =
        caseDetailService.getOffences(crn, eventNumber)

    @GetMapping(value = ["/case/{crn}/registrations"])
    fun getRegistrations(@PathVariable crn: String) =
        caseDetailService.getRegistrations(crn)

    @GetMapping(value = ["/case/{crn}/requirement/{id}"])
    fun getRequirement(@PathVariable crn: String, @PathVariable id: Long) =
        caseDetailService.getRequirement(id)

    @GetMapping(value = ["/case/{crn}/licence-conditions/{id}"])
    fun getLicenceCondition(@PathVariable crn: String, @PathVariable id: Long) =
        caseDetailService.getLicenceCondition(id)
}
