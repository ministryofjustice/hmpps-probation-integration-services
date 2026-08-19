package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CommunityManagerService

@RestController
class CommunityManagerController(
    private val communityManagerService: CommunityManagerService
) {
    @PreAuthorize("hasRole('PROBATION_API__COMMUNITY_SUPPORT_INTERVENTIONS__CASE_DETAIL')")
    @GetMapping(value = ["/case/{crn}/community-manager"])
    fun getCommunityManagerByCrn(@PathVariable("crn") crn: String) =
        communityManagerService.getCommunityManagerForCrn(crn)
}