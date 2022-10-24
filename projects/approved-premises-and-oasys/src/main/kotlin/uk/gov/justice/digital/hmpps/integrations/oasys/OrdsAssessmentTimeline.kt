package uk.gov.justice.digital.hmpps.integrations.oasys

data class OrdsAssessmentTimeline(
    var source: String,
    var inputs: Inputs,
    var crn: String,
    var limitedAccessOffender: Boolean,
    var timeline: List<OasysAssessment>
)

data class Inputs(
    val crn: String,
    val laoPrivilege: String
)
