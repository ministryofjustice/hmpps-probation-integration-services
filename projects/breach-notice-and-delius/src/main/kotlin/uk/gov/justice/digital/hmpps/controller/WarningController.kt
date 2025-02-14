package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.WarningDetails
import uk.gov.justice.digital.hmpps.model.WarningTypes
import uk.gov.justice.digital.hmpps.service.WarningService
import java.util.*

@RestController
class WarningController(private val warningService: WarningService) {
    @PreAuthorize("hasRole('PROBATION_API__BREACH_NOTICE__CASE_DETAIL')")
    @GetMapping(value = ["/warning-types"])
    fun getWaringTypes(): WarningTypes = warningService.getWarningTypes()

    @PreAuthorize("hasRole('PROBATION_API__BREACH_NOTICE__CASE_DETAIL')")
    @GetMapping(value = ["/warning-details/{crn}/{breachNoticeId}"])
    fun getWaringTypes(@PathVariable crn: String, @PathVariable breachNoticeId: UUID): WarningDetails =
        warningService.getWarningDetails(crn, breachNoticeId)
}