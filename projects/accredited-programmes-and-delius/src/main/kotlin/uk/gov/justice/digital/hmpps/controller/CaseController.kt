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
}
