package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ReferenceDataService

@RestController
@RequestMapping("/reference-data")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class ReferenceDataController(
    private val referenceDataService: ReferenceDataService
) {
    @GetMapping(value = ["/project-types"])
    fun getProjectTypes() = referenceDataService.getProjectTypes()

    @GetMapping("/unpaid-work-appointment-outcomes")
    fun getUnpaidWorkAppointmentOutcomes() = referenceDataService.getUpwAppointmentOutcomes()
}