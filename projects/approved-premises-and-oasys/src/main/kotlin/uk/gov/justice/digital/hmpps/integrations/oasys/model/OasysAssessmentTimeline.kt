package uk.gov.justice.digital.hmpps.integrations.oasys.model

data class OasysAssessmentTimeline(
    val source: String,
    val inputs: Inputs,
    val crn: String,
    val limitedAccessOffender: Boolean,
    val timeline: List<OasysTimelineAssessment> = emptyList(),
)

data class Inputs(
    val crn: String,
    val laoPrivilege: String,
)
