package uk.gov.justice.digital.hmpps.model

data class Teams(
    val teams: List<Team>
)

data class Team(
    val code: String,
    val description: String,
    val pdu: CodedValue,
    val region: CodedValue,
)