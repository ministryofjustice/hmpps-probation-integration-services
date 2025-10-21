package uk.gov.justice.digital.hmpps.service

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.integration.probationsearch.ProbationSearchClient
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class ProbationCaseSearch(
    private val personRepository: DetailRepository,
    private val searchClient: ProbationSearchClient,
    private val telemetry: TelemetryService
) {
    fun find(request: SearchRequest, useSearch: Boolean): List<OffenderDetail> {
        val psResult = searchClient.findAll(request).map {
            it.copy(
                offenderAliases = emptyList(),
                offenderManagers = it.offenderManagers?.filter { it.active == true }
            )
        }

        val dbResult = try {
            val dbResult = personRepository.findAll(request.asSpecification()).map { it.toProbationCase(false) }

            if (dbResult.toSet() != psResult.toSet()) {
                telemetry.trackEvent(
                    "SearchMismatch",
                    mapOf(
                        "searchFields" to request.fields().joinToString(","),
                        "resultsSize" to "${dbResult.size} / ${psResult.size}",
                        "searchResults" to psResult.joinToString(",") { it.otherIds.crn },
                        "dbResults" to dbResult.joinToString(",") { it.otherIds.crn }
                    )
                )
            }
            dbResult
        } catch (ex: Exception) {
            telemetry.trackException(ex)
            null
        }

        return if (useSearch) psResult else dbResult ?: psResult
    }

    fun crns(crns: Set<String>): List<OffenderDetail> =
        personRepository.findByCrnIn(crns).map { it.toProbationCase(true) }
}

private fun SearchRequest.asSpecification(): Specification<DetailPerson> = listOfNotNull(
    firstName?.let { matchesForename(it) },
    surname?.let { matchesSurname(it) },
    dateOfBirth?.let { matchesDateOfBirth(it) },
    crn?.let { matchesCrn(it) },
    nomsNumber?.let { matchesNomsId(it) },
    pncNumber?.let { matchesPnc(it) }
).ifEmpty { throw IllegalArgumentException("At least one field must be provided") }
    .reduce { a, b -> a.and(b) }

private fun DetailPerson.toProbationCase(includeAliases: Boolean) = OffenderDetail(
    firstName = forename,
    middleNames = listOfNotNull(secondName, thirdName),
    surname = surname,
    dateOfBirth = dateOfBirth,
    gender = gender.description,
    otherIds = IDs(crn, nomsNumber, pncNumber),
    offenderProfile = OffenderProfile(ethnicity?.description, nationality?.description, religion?.description),
    offenderManagers = personManager.map { it.asOffenderManager() },
    offenderAliases = if (includeAliases) offenderAliases.map { it.asProbationAlias() } else emptyList(),
    mappa = mappaRegistrations.firstOrNull()?.asMappaDetails()
)

fun Registration.asMappaDetails() = MappaDetails(
    level = when (level?.code) {
        "M1" -> 1; "M2" -> 2; "M3" -> 3; else -> 0
    },
    levelDescription = level?.description ?: "Missing Level",
    category = when (category?.code) {
        "M1" -> 1; "M2" -> 2; "M3" -> 3; "M4" -> 4; else -> 0
    },
    categoryDescription = category?.description ?: "Missing category",
    startDate = date,
    reviewDate = nextReviewDate,
    team = KeyValue(team.code, team.description),
    officer = staff.toStaffHuman(),
    probationArea = KeyValue(team.probationArea.code, team.probationArea.description),
    notes = notes
)

private fun PersonManager.asOffenderManager() = OffenderManager(
    staff = staff.toStaffHuman(),
    team = SearchResponseTeam(
        team.code,
        team.description,
        KeyValue(team.district.code, team.district.description),
    ),
    probationArea = ProbationArea(probationArea.code, probationArea.description, listOf()),
    active = active,
)

private fun PersonAlias.asProbationAlias() = OffenderAlias(
    id = aliasID,
    dateOfBirth = dateOfBirth,
    firstName = firstName,
    middleNames = listOfNotNull(secondName, thirdName),
    surname = surname,
    gender = gender.description
)