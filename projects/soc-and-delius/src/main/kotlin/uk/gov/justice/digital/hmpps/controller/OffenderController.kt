package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ConvictionService
import uk.gov.justice.digital.hmpps.service.OffenderService

@RestController
class OffenderController(private val offenderService: OffenderService) {

    @PreAuthorize("hasRole('ROLE_SOC_PROBATION_CASE')")
    @GetMapping(value = ["offender/{crn}/probation-record"])
    fun convictions(
        @PathVariable crn: String,
    ) = offenderService.getProbationRecord(crn)
}
