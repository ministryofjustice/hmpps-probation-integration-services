package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.PersonalCircumstanceType
import uk.gov.justice.digital.hmpps.integrations.delius.person.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.CaseOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Registration
import uk.gov.justice.digital.hmpps.model.*

@Service
class CaseService(
    private val probationCaseRepository: ProbationCaseRepository,
    private val registrationRepository: RegistrationRepository,
    private val offenceRepository: MainOffenceRepository,
    private val personalCircumstanceRepository: PersonalCircumstanceRepository
) {
    fun getCaseSummaries(crns: List<String>): CaseSummaries =
        CaseSummaries(probationCaseRepository.findByCrnIn(crns).map { it.summary() })

    fun getCaseDetail(crn: String): CaseDetail {
        val person = probationCaseRepository.findByCrn(crn)
            ?: throw uk.gov.justice.digital.hmpps.exception.NotFoundException("ProbationCase", "crn", crn)
        val registrations = registrationRepository.findByPersonId(person.id)
        val offences = offenceRepository.findOffencesFor(person.id)
        val circumstances = personalCircumstanceRepository.findByPersonId(person.id)
        return person.summary().withDetail(offences, registrations, circumstances.map { it.type.code })
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

fun CaseSummary.withDetail(
    offences: List<CaseOffence>,
    registrations: List<Registration>,
    circumstances: List<String>
): CaseDetail {
    val regMap = registrations.groupBy { it.type.code == RegisterType.Code.MAPPA.value }
    return CaseDetail(
        this,
        offences.map { it.asOffence() },
        regMap.flags(),
        regMap.mappa(),
        circumstances.isCareLeaver(),
        circumstances.isVeteran()
    )
}

fun List<String>.isCareLeaver() = contains(PersonalCircumstanceType.Code.CARE_LEAVER.value)
fun List<String>.isVeteran() = contains(PersonalCircumstanceType.Code.VETERAN.value)

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

fun CommunityManager.team() = Team(
    team.code,
    team.description,
    Ldu(team.ldu.code, team.ldu.description),
    Borough(team.ldu.borough.code, team.ldu.borough.description),
    team.startDate,
    team.endDate
)

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

fun Map<Boolean, List<Registration>>.mappa() = get(true)?.firstOrNull {
    it.category?.code != null && Category.entries.map(Category::name).contains(it.category.code)
}?.asMappa()
fun Map<Boolean, List<Registration>>.flags() = get(false)?.map { it.asRegistration() } ?: listOf()
