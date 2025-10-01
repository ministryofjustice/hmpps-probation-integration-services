package uk.gov.justice.digital.hmpps.model

data class UserTeams(val teams: List<UserTeam>)
data class UserTeam(val code: String, val description: String, val pdu: CodedValue, val region: CodedValue)