package uk.gov.justice.digital.hmpps.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.oasys.OrdsClient

@RestController
@RequestMapping("assessments")
class AssessmentController(private val ordsClient: OrdsClient) {
    @PreAuthorize("hasRole('ROLE_PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
    @GetMapping("/timeline/{nomsId}")
    fun getTimeline(@PathVariable nomsId: String): Timeline = ordsClient.getTimeline(nomsId)

    @PreAuthorize("hasRole('ROLE_PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
    @GetMapping("/{id}/section/{name}")
    fun getSection(@PathVariable id: Long, @PathVariable name: String): JsonNode =
        ordsClient.getSection(id, name.lowercase()).asResponse()
}

private fun ObjectNode.asResponse(): JsonNode {
    val assessment = this["assessments"].first() as ObjectNode
    this["probNumber"]?.let { assessment.set<JsonNode>("crn", it) }
    this["prisNumber"]?.let { assessment.set<JsonNode>("nomsId", it) }
    return assessment
}