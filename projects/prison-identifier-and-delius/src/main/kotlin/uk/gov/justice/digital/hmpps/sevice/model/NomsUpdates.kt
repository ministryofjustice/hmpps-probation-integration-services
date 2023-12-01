package uk.gov.justice.digital.hmpps.sevice.model

class NomsUpdates(
    var personMatches: List<PersonMatch>
)

class PersonMatch(
    val crn: String,
    val nomsNumber: String?,
    val matchDetail: MatchDetail?
)

class MatchDetail(
    val message: String,
    val potentialMatches: List<String>
)
