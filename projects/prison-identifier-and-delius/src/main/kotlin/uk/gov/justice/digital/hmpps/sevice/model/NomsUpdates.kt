package uk.gov.justice.digital.hmpps.sevice.model

class NomsUpdates(
    val personMatches: List<PersonMatch>
)

data class PersonMatch(
    val crn: String,
    val matchedNomsNumber: String?,
    val matchDetail: MatchDetail?
)

class MatchDetail(
    val message: String,
    val potentialMatches: List<String>?
)
