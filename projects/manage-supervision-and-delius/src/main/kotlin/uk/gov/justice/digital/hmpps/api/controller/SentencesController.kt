package uk.gov.justice.digital.hmpps.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.service.SentenceService

@RestController
@Tag(name = "Sentences")
@RequestMapping("/sentences/{crn}")
@PreAuthorize("hasRole('PROBATION_API__MANAGE_A_SUPERVISION__CASE_DETAIL')")
class SentencesController(private val sentenceService: SentenceService) {

    //TODO remove this api after f/e integration with getProbationRecordsByContactType
    @GetMapping
    @Operation(summary = "Display active events")
    fun getOverview(
        @PathVariable crn: String,
        @RequestParam(required = false) number: String?,
        @RequestParam(required = false, defaultValue = "true") includeRarRequirements: Boolean = true
    ) = sentenceService.getActiveSentences(crn, includeRarRequirements)
}