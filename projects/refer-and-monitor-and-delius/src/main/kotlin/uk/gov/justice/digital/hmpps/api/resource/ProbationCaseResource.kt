package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.service.ManagerService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationCaseResource(private val managerService: ManagerService) {
    @PreAuthorize("hasRole('CRS_REFERRAL')")
    @GetMapping("responsible-officer")
    fun findResponsibleOfficer(@PathVariable crn: String): ResponsibleOfficer =
        managerService.findResponsibleCommunityManager(crn)
}
