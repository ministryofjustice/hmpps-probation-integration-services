package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ResponsibleOfficerService

@RestController
class ResponsibleOfficerController(val responsibleOfficerService: ResponsibleOfficerService) {
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping(value = ["/responsible-officer-details/{crn}"])
    fun getResponsibleOfficerDetails(@PathVariable crn: String) = responsibleOfficerService.getResponsibleOfficerDetails(crn)
}