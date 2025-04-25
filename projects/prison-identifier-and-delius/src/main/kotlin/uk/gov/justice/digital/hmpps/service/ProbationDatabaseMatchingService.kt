package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.client.*
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class ProbationDatabaseMatchingService(
    private val searchRepository: ProbationSearchRepository,
    private val telemetryService: TelemetryService,
) {
    fun match(request: ProbationMatchRequest): ProbationMatchResponse {

        performSearch(searchRepository::fullSearch, request, "ALL_SUPPLIED")?.let { response -> return response }
        performSearch(
            searchRepository::fullSearchAlias,
            request,
            "ALL_SUPPLIED_ALIAS"
        )?.let { response -> return response }
        performSearch(searchRepository::searchByNoms, request, "HMPPS_KEY")?.let { response -> return response }
        performSearch(searchRepository::searchByCro, request, "EXTERNAL_KEY")?.let { response -> return response }
        performSearch(searchRepository::searchByPnc, request, "EXTERNAL_KEY")?.let { response -> return response }
        performSearch(searchRepository::searchByName, request, "NAME")?.let { response -> return response }
        performSearch(
            searchRepository::searchByPartialName,
            request,
            "PARTIAL_NAME"
        )?.let { response -> return response }
        performSearch(
            searchRepository::searchByPartialNameLenientDob,
            request,
            "PARTIAL_NAME_DOB_LENIENT"
        )?.let { response -> return response }
        return ProbationMatchResponse(matches = emptyList(), matchedBy = "NOTHING")
    }

    fun performSearch(
        fn: (request: ProbationMatchRequest) -> List<Person>,
        request: ProbationMatchRequest,
        matchedBy: String
    ): ProbationMatchResponse? {
        val results = fn(request)
        if (results.isNotEmpty()) {
            return ProbationMatchResponse(
                matches = results.map { OffenderMatch(it.toOffenderDetail()) },
                matchedBy = matchedBy
            )
        }
        return null
    }

    fun performCompare(request: ProbationMatchRequest, apiMatchResponse: ProbationMatchResponse) {
        val dbMatchResponse = match(request)
        val dbCrns = dbMatchResponse.matches.map { it.offender.otherIds.crn }.toSet()
        val apiCrns = apiMatchResponse.matches.map { it.offender.otherIds.crn }.toSet()
        if (dbCrns != apiCrns) {
            //Difference found
            telemetryService.trackEvent(
                "MatchDifferenceFound",
                mapOf(
                    "nomsNumber" to request.nomsNumber,
                    "apiMatches" to apiCrns.joinToString(","),
                    "dbMatches" to dbCrns.joinToString(","),
                    "dbMatchedBy" to dbMatchResponse.matchedBy,
                    "apiMatchedBy" to apiMatchResponse.matchedBy
                )
            )
        }
    }
}

fun Person.toOffenderDetail() = OffenderDetail(
    otherIds = IDs(
        crn = crn,
        croNumber = croNumber,
        pncNumber = pncNumber,
        nomsNumber = nomsNumber,
        mostRecentPrisonerNumber = mostRecentPrisonerNumber,
        immigrationNumber = immigrationNumber,
        niNumber = niNumber
    ),
    previousSurname = previousSurname,
    title = title?.description,
    firstName = forename,
    middleNames = listOfNotNull(secondName, forename),
    surname = surname,
    dateOfBirth = dateOfBirth,
    gender = gender?.description,
    currentDisposal = if (currentDisposal) "1" else "0",
)
