package uk.gov.justice.digital.hmpps.integrations.oasys

import org.springframework.web.service.annotation.GetExchange
import java.net.URI
import java.time.LocalDate

interface OrdsClient {
    @GetExchange
    fun getAssessmentSummary(uri: URI): AssessmentSummary
}

data class AssessmentSummary(
    val crn: String,
    val eventNumber: String,
    val oasysId: String,
    val date: LocalDate,
    val description: String,
    val ogrs3Score1: Long?,
    val ogrs3Score2: Long?,
    val riskFlags: String?
)
