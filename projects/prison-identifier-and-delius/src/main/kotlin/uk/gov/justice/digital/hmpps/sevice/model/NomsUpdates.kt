package uk.gov.justice.digital.hmpps.sevice.model

import java.time.LocalDate

class NomsUpdates(
    val personMatches: List<PersonMatch>
)

data class PersonMatch(
    val crn: String,
    val matchDetail: MatchDetail?,
    val matchReason: MatchReason
)

data class MatchReason(
    val message: String,
    val potentialMatches: List<MatchDetail> = listOf()
)
data class MatchDetail(
    val nomsNumber: String,
    val bookingRef: String,
    val sentenceDate: LocalDate
)
