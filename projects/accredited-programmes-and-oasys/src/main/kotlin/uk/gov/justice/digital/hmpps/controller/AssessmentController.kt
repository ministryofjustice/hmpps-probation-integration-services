package uk.gov.justice.digital.hmpps.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.integrations.oasys.OrdsClient
import uk.gov.justice.digital.hmpps.integrations.oasys.asIntegrationModel
import uk.gov.justice.digital.hmpps.integrations.oasys.getRiskPredictors

@RestController
@RequestMapping("assessments")
class AssessmentController(private val ordsClient: OrdsClient) {
    @PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
    @GetMapping("/timeline/{nomsId}")
    fun getTimeline(@PathVariable nomsId: String): Timeline = ordsClient.getTimeline(nomsId)

    @PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
    @GetMapping("/{id}/section/{name}")
    fun getSection(@PathVariable id: Long, @PathVariable name: String): JsonNode =
        ordsClient.getSection(id, name.lowercase()).asResponse()

    @PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
    @GetMapping("/{id}/risk-predictors")
    fun getRiskPredictors(@PathVariable id: Long): RiskPrediction = ordsClient.getRiskPredictors(id)

    @PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
    @GetMapping("/pni/{nomsId}")
    fun getPniCalculation(
        @PathVariable nomsId: String,
        @RequestParam community: Boolean
    ): PniResponse = ordsClient.getPni(nomsId, if (community) "Y" else "N").asIntegrationModel()

    @ExceptionHandler
    fun handleNotFound(e: HttpClientErrorException) = ResponseEntity
        .status(e.statusCode)
        .body(ErrorResponse(status = e.statusCode.value(), message = e.message))
}

private fun ObjectNode.asResponse(): JsonNode {
    val assessment = this["assessments"].first() as ObjectNode
    this["probNumber"]?.let { with(assessment) { set<JsonNode>("crn", it) } }
    this["prisNumber"]?.let { with(assessment) { set<JsonNode>("nomsId", it) } }
    return assessment
}