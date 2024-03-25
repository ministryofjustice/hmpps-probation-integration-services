package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.SentenceService

@RestController
@Tag(name = "Sentence")
@RequestMapping("/sentence/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class SentenceController(private val sentenceService: SentenceService) {

    @GetMapping
    @Operation(summary = "Display the most recent ‘Active Event’ ")
    fun getOverview(@PathVariable crn: String) = sentenceService.getMostRecentActiveEvent(crn)
}