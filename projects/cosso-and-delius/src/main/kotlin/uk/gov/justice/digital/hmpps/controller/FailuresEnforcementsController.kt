package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.FailureAndEnforcementService

@RestController
class FailuresEnforcementsController(private val failuresAndEnforcementService: FailureAndEnforcementService) {
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping("/failures-enforcements/{crn}/{cossoId}")
    fun getFailuresAndEnforcement(
        @PathVariable crn: String,
        @PathVariable cossoId: String
    ) = failuresAndEnforcementService.getFailuresAndEnforcement(crn, cossoId)
}