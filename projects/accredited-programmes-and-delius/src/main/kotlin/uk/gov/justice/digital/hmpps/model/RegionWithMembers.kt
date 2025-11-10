package uk.gov.justice.digital.hmpps.model

data class RegionWithMembers(
    val code: String,
    val description: String,
    val pdus: List<Pdu>
) {
    data class Pdu(val code: String, val description: String, val team: List<Team>)
    data class Team(val code: String, val description: String, val members: List<Member>) {
        data class Member(val code: String, val name: Name)
    }
}