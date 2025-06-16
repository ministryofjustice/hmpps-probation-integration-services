package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class MatchResponse(val matches: List<Match>, val matchedBy: MatchedBy)

data class Match(val offender: MatchedPerson)

data class MatchedPerson(
    val firstName: String,
    val surname: String,
    val dateOfBirth: LocalDate,
    val otherIds: Ids,
    val probationStatus: ProbationStatusDetail,
    val offenderAliases: List<OffenderAlias> = emptyList(),
)

enum class MatchedBy {
    ALL_SUPPLIED,
    ALL_SUPPLIED_ALIAS,
    EXTERNAL_KEY,
    NAME,
    PARTIAL_NAME,
    PARTIAL_NAME_DOB_LENIENT,
    NOTHING
}

data class MatchRequest(
    val surname: String,
    val pncNumber: String? = null,
    val firstName: String? = null,
    val dateOfBirth: LocalDate? = null,
    val activeSentence: Boolean = false,
)