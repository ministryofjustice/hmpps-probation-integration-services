package uk.gov.justice.digital.hmpps.client

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import java.time.LocalDate

interface ProbationSearchClient {
    @PostExchange(url = "/match")
    fun match(@RequestBody body: ProbationMatchRequest): ProbationMatchResponse
}

data class ProbationMatchRequest(
    val firstName: String,
    val surname: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate,
    val nomsNumber: String,
    val activeSentence: Boolean = true,
    val pncNumber: String? = null,
    val croNumber: String? = null,
) {
    constructor(prisoner: Prisoner) : this(
        firstName = prisoner.firstName,
        surname = prisoner.lastName,
        dateOfBirth = prisoner.dateOfBirth,
        nomsNumber = prisoner.offenderNo,
        pncNumber = prisoner.pncNumber,
        croNumber = prisoner.croNumber,
        activeSentence = true,
    )
}

data class ProbationMatchResponse(
    val matches: List<OffenderMatch>,
    val matchedBy: String,
)

data class OffenderMatch(
    val offender: OffenderDetail,
)

data class OffenderDetail(
    val otherIds: IDs,
    val previousSurname: String? = null,
    val title: String? = null,
    val firstName: String? = null,
    val middleNames: List<String>? = null,
    val surname: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val currentDisposal: String? = null,
)

data class IDs(
    val crn: String,
    val pncNumber: String? = null,
    val croNumber: String? = null,
    val niNumber: String? = null,
    val nomsNumber: String? = null,
    val immigrationNumber: String? = null,
    val mostRecentPrisonerNumber: String? = null,
)
