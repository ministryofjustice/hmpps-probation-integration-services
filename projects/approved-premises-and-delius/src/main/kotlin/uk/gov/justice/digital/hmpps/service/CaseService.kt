package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.person.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.model.CaseSummaries
import uk.gov.justice.digital.hmpps.model.CaseSummary
import uk.gov.justice.digital.hmpps.model.Manager
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.Profile
import uk.gov.justice.digital.hmpps.model.Team

@Service
class CaseService(private val probationCaseRepository: ProbationCaseRepository) {
    fun getCaseSummaries(crns: List<String>): CaseSummaries =
        CaseSummaries(probationCaseRepository.findByCrnIn(crns).map { it.summary() })
}

fun ProbationCase.summary() = CaseSummary(
    crn,
    nomsId,
    name(),
    dateOfBirth,
    gender?.description,
    profile(),
    manager(),
    currentExclusion ?: false,
    currentRestriction ?: false
)

fun ProbationCase.name() = Name(forename, surname, listOfNotNull(secondName, thirdName))
fun ProbationCase.profile() =
    Profile(ethnicity?.description, genderIdentity?.description, nationality?.description, religion?.description)

fun ProbationCase.manager(): Manager =
    with(currentManager()) {
        Manager(team())
    }

fun CommunityManager.team() = Team(team.code, team.description)
