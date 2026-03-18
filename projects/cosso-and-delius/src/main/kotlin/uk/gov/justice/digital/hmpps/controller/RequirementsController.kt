package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.RequirementsService

@RestController
class RequirementsController(private val requirementsService: RequirementsService) {
    @GetMapping("/requirements/{breachNoticeId}")
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    fun getRequirements(@PathVariable breachNoticeId: String) =
        requirementsService.getRequirements(breachNoticeId)
}