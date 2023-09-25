package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.service.ManagerService

@RestController
@RequestMapping("probation-case/{crn}")
class ProbationCaseResource(private val responsibleManagerService: ManagerService) {
    @PreAuthorize("hasRole('CVL_CONTEXT')")
    @GetMapping("responsible-community-manager")
    fun handle(@PathVariable crn: String): ResponseEntity<Manager> =
        responsibleManagerService.findCommunityManager(crn)?.let {
            ResponseEntity.ok().body(it)
        } ?: ResponseEntity.noContent().build()
}
