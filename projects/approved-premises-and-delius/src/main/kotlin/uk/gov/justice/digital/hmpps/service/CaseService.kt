package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.person.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.CaseOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Category
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Level
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.model.CaseDetail
import uk.gov.justice.digital.hmpps.model.CaseSummaries
import uk.gov.justice.digital.hmpps.model.CaseSummary
import uk.gov.justice.digital.hmpps.model.Ldu
import uk.gov.justice.digital.hmpps.model.Manager
import uk.gov.justice.digital.hmpps.model.MappaDetail
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.Offence
import uk.gov.justice.digital.hmpps.model.Profile
import uk.gov.justice.digital.hmpps.model.Team

@Service
class CaseService(
    private val probationCaseRepository: ProbationCaseRepository,
    private val registrationRepository: RegistrationRepository,
    private val offenceRepository: MainOffenceRepository
) {
    fun getCaseSummaries(crns: List<String>): CaseSummaries =
        CaseSummaries(probationCaseRepository.findByCrnIn(crns).map { it.summary() })

    fun getCaseDetail(crn: String): CaseDetail {
        val person = probationCaseRepository.findByCrn(crn)
            ?: throw uk.gov.justice.digital.hmpps.exception.NotFoundException("ProbationCase", "crn", crn)
        val registrations = registrationRepository.findByPersonId(person.id)
        val offences = offenceRepository.findOffencesFor(person.id)
        return person.summary().withDetail(offences, registrations)
    }
}

fun ProbationCase.summary() = CaseSummary(
    crn,
    nomsId,
    pnc,
    name(),
    dateOfBirth,
    gender?.description,
    profile(),
    manager(),
    currentExclusion ?: false,
    currentRestriction ?: false
)

fun CaseSummary.withDetail(offences: List<CaseOffence>, registrations: List<Registration>): CaseDetail {
    val regMap = registrations.groupBy { it.type.code == RegisterType.Code.MAPPA.value }
    return CaseDetail(this, offences.map { it.asOffence() }, regMap.flags(), regMap.mappa())
}

fun ProbationCase.name() = Name(forename, surname, listOfNotNull(secondName, thirdName))
fun ProbationCase.profile() =
    Profile(
        ethnicity?.description,
        genderIdentityDescription ?: genderIdentity?.description,
        nationality?.description,
        religion?.description
    )

fun ProbationCase.manager(): Manager =
    with(currentManager()) {
        Manager(team())
    }

fun CommunityManager.team() = Team(team.code, team.description, Ldu(team.ldu.code, team.ldu.description))

fun CaseOffence.asOffence() = Offence(code, description, date, main, eventNumber)

fun Registration.asRegistration() = uk.gov.justice.digital.hmpps.model.Registration(type.code, type.description, date)
fun Registration.asMappa() = MappaDetail(
    level?.code?.toMappaLevel(),
    level?.description,
    category?.code?.toMappaCategory(),
    category?.description,
    date,
    lastUpdatedDatetime
)

fun String.toMappaLevel() = Level.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA level: $this")

fun String.toMappaCategory() = Category.entries.find { it.name == this }?.number
    ?: throw IllegalStateException("Unexpected MAPPA category: $this")

fun Map<Boolean, List<Registration>>.mappa() = get(true)?.firstOrNull()?.asMappa()
fun Map<Boolean, List<Registration>>.flags() = get(false)?.map { it.asRegistration() } ?: listOf()
