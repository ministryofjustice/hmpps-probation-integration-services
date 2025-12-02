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
@PreAuthorize("hasRole('PROBATION_API__ACCREDITED_PROGRAMMES__ASSESSMENT')")
class AssessmentController(private val ordsClient: OrdsClient) {
    @GetMapping("/timeline/{nomisIdOrCrn}")
    fun getTimeline(@PathVariable nomisIdOrCrn: String): Timeline =
        ordsClient.getTimeline(nomisIdOrCrn.idType(), nomisIdOrCrn)

    @GetMapping("/{id}/section/{name}")
    fun getSection(@PathVariable id: Long, @PathVariable name: String): JsonNode =
        ordsClient.getSection(id, name.lowercase()).asResponse()

    @Deprecated("Use /assessments/id/{id}/risk/predictors/all in the Assess Risks and Needs (ARNS) API. This endpoint will be removed in a future release.")
    @GetMapping("/{id}/risk-predictors")
    fun getRiskPredictors(@PathVariable id: Long): RiskPrediction = ordsClient.getRiskPredictors(id)

    @GetMapping("/pni/{nomisIdOrCrn}")
    fun getPniCalculation(@PathVariable nomisIdOrCrn: String, @RequestParam community: Boolean): PniResponse =
        ordsClient.getPni(nomisIdOrCrn.idType(), nomisIdOrCrn, if (community) "Y" else "N").asIntegrationModel()

    @ExceptionHandler
    fun handleNotFound(e: HttpClientErrorException) = ResponseEntity
        .status(e.statusCode)
        .body(ErrorResponse(status = e.statusCode.value(), message = e.message))

    private fun String.idType() = when {
        matches(Regex("^[A-Za-z][0-9]{4}[A-Za-z]{2}$")) -> "pris"
        matches(Regex("^[A-Za-z][0-9]{6}$")) -> "prob"
        else -> throw IllegalArgumentException("Invalid CRN or NOMIS ID: $this")
    }
}

private fun ObjectNode.asResponse(): JsonNode {
    val assessment = this["assessments"].first() as ObjectNode
    this["probNumber"]?.let { with(assessment) { set<JsonNode>("crn", it) } }
    this["prisNumber"]?.let { with(assessment) { set<JsonNode>("nomsId", it) } }
    return assessment
}