package uk.gov.justice.digital.hmpps.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.manager.ManagerService

@RestController
@RequestMapping("probation-cases/{nomsId}")
class ManagerResource(private val managerService: ManagerService) {
    @PreAuthorize("hasRole('PROBATION_API__PRISONER_PROFILE__CASE_DETAIL')")
    @RequestMapping("community-manager", method = [RequestMethod.GET])
    fun findCommunityManager(
        @PathVariable nomsId: String,
    ): Manager = managerService.findCommunityManager(nomsId)
}
