package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.PersonalDetailsService
import uk.gov.justice.digital.hmpps.service.SentenceService

@RestController
@PreAuthorize("hasRole('PROBATION_API__MANAGE_MY_COMMUNITY_SENTENCE__PERSONAL_DETAILS')")
class ApiController(
    private val personalDetailsService: PersonalDetailsService,
    private val sentenceService: SentenceService
) {
    @GetMapping(value = ["/person/{crn}/name"])
    fun getName(@PathVariable crn: String) = personalDetailsService.getName(crn)

    @GetMapping(value = ["/person/{crn}/personal-details"])
    fun getPersonalDetails(@PathVariable crn: String) = personalDetailsService.getPersonalDetails(crn)

    @GetMapping(value = ["/person/{crn}/sentences"])
    fun getConditions(@PathVariable crn: String) = sentenceService.getSentences(crn)
}
