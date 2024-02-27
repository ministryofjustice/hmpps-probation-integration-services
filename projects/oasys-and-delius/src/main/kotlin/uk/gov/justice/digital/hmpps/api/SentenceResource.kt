package uk.gov.justice.digital.hmpps.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.SentenceService

@RestController
@RequestMapping("probation-cases/{crn}")
class SentenceResource(private val sentenceService: SentenceService) {
    @PreAuthorize("hasRole('PROBATION_API__OASYS__CASE_DETAIL')")
    @GetMapping(value = ["/release"])
    fun getReleaseRecall(@PathVariable("crn") crn: String) = sentenceService.findLatestReleaseRecall(crn)
}