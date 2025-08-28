package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.RequirementResponse
import uk.gov.justice.digital.hmpps.service.RequirementService
import java.util.*

@RestController
class RequirementsController(private val requirementService: RequirementService) {
    @PreAuthorize("hasRole('PROBATION_API__BREACH_NOTICE__CASE_DETAIL')")
    @GetMapping(value = ["/requirements/{breachNoticeId}"])
    fun getWarningDetails(@PathVariable breachNoticeId: UUID): RequirementResponse =
        requirementService.getRequirements(breachNoticeId)
}