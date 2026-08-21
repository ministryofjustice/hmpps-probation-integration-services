package uk.gov.justice.digital.hmpps.model

data class AccreditedProgrammesMembersResponse(
    val teams: List<Team>
) {
    data class Team(
        val code: String,
        val description: String,
        val members: List<Member>
    ) {
        data class Member(
            val code: String,
            val name: Name
        )
    }
}


