package uk.gov.justice.digital.hmpps.service

import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.delius.entity.*
import uk.gov.justice.digital.hmpps.integration.probationsearch.ProbationSearchClient
import uk.gov.justice.digital.hmpps.integration.probationsearch.asCaseAlias
import uk.gov.justice.digital.hmpps.integration.probationsearch.asProfile
import uk.gov.justice.digital.hmpps.integration.probationsearch.ids
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class ProbationCaseSearch(
    private val personRepository: PersonRepository,
    private val searchClient: ProbationSearchClient,
    private val telemetry: TelemetryService,
) {
    fun find(request: SearchRequest, useSearch: Boolean): List<ProbationCaseDetail> {
        val psResult = searchClient.findAll(request).map { o ->
            ProbationCaseDetail(
                o.otherIds.ids(),
                o.firstName,
                o.surname,
                o.dateOfBirth,
                o.gender ?: "",
                o.middleNames ?: emptyList(),
                o.offenderProfile?.asProfile() ?: CaseProfile(),
                o.contactDetails,
                o.offenderAliases?.map { a -> a.asCaseAlias() } ?: emptyList(),
                o.activeProbationManagedSentence == true,
                o.currentRestriction == true,
                o.restrictionMessage,
                o.currentExclusion == true,
                o.exclusionMessage,
            )
        }
        val dbResult = try {
            val dbResult = personRepository.findAll(request.asSpecification())
                .map { p ->
                    ProbationCaseDetail(
                        p.ids(),
                        p.forename,
                        p.surname,
                        p.dateOfBirth,
                        p.gender.description,
                        listOfNotNull(p.secondName, p.thirdName),
                        p.profile(),
                        p.contactDetails(),
                        p.aliases.map { a -> a.asAlias() },
                        p.currentDisposal,
                        p.currentRestriction == true,
                        p.restrictionMessage,
                        p.currentExclusion == true,
                        p.exclusionMessage,
                    )
                }

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

    private fun SearchRequest.asSpecification(): Specification<Person> = listOfNotNull(
        if (includeAliases) {
            matchesPerson(firstName, surname, dateOfBirth).or(matchesAlias(firstName, surname, dateOfBirth))
        } else {
            matchesPerson(firstName, surname, dateOfBirth)
        },
        crn?.let { matchesCrnOrPrevious(it) },
        nomsNumber?.let { matchesNomsId(it) },
        pncNumber?.let { matchesPnc(it) }
    ).ifEmpty { throw IllegalArgumentException("At least one field must be provided") }
        .reduce { a, b -> a.and(b) }
}

fun Person.ids() = OtherIds(crn, pnc, nomsId, cro)
fun Person.profile() =
    CaseProfile(
        ethnicity?.description,
        nationality?.description,
        religion?.description,
        sexualOrientation?.description,
        disabilities.map { it.asCaseDisability() }
    )

fun Disability.asCaseDisability() = CaseDisability(
    CodedValue(type.code, type.description),
    condition?.let { CodedValue(it.code, it.description) },
    startDate,
    finishDate,
    notes
)

fun Person.contactDetails() = ContactDetails(
    listOfNotNull(
        telephoneNumber?.let { PhoneNumber(it, "TELEPHONE") },
        mobileNumber?.let { PhoneNumber(it, "MOBILE") },
    ),
    listOfNotNull(emailAddress)
)

fun PersonAlias.asAlias() =
    CaseAlias(firstName, surname, dateOfBirth, gender.description, listOfNotNull(secondName, thirdName))
