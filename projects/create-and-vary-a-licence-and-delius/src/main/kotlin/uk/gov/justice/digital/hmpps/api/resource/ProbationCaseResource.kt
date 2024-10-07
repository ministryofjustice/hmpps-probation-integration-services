package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.StaffEmail
import uk.gov.justice.digital.hmpps.service.ManagerService

@RestController
@RequestMapping("probation-case")
@PreAuthorize("hasRole('PROBATION_API__CVL__CASE_DETAIL')")
class ProbationCaseResource(private val responsibleManagerService: ManagerService) {
    @GetMapping("{crn}/responsible-community-manager")
    fun findCommunityManager(@PathVariable crn: String): Manager = responsibleManagerService.findCommunityManager(crn)

    @PostMapping("/responsible-community-manager")
    fun findCommunityManagerEmails(@RequestBody crns: List<String>): List<StaffEmail> =
        responsibleManagerService.findCommunityManagerEmails(crns)
}
