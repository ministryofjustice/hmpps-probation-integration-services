package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController(private val service: ResettlementPassportService) {
    @PreAuthorize("hasRole('ROLE_RESETTLEMENT_PASSPORT')")
    @GetMapping(value = ["/duty-to-refer-nsi/{crn}"])
    fun handle(
        @PathVariable("crn") crn: String
    ) = service.getDutyToReferNSI(crn)
}
