package uk.gov.justice.digital.hmpps.controller

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDateTime

data class Timeline(
    @JsonAlias("probNumber") val crn: String?,
    @JsonAlias("prisNumber") val nomsId: String?,
    val timeline: List<AssessmentSummary>
)

data class AssessmentSummary(
    @JsonAlias("assessmentPk")
    val id: Long,
    @JsonAlias("completedDate")
    val completedAt: LocalDateTime?,
    @JsonAlias("assessmentType")
    val type: String,
    val status: String,
)