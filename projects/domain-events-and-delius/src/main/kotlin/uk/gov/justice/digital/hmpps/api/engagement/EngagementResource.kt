package uk.gov.justice.digital.hmpps.api.engagement

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.Engagement
import uk.gov.justice.digital.hmpps.service.PersonService

@RestController
@RequestMapping
class EngagementResource(private val personService: PersonService) {
    @PreAuthorize("hasRole('VIEW_PROBATION_CASE_ENGAGEMENT_CREATED')")
    @GetMapping("probation-case.engagement.created/{crn}")
    fun getEngagement(@PathVariable crn: String): Engagement = personService.findEngagement(crn)
}