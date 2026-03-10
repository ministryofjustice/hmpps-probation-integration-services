package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCase
import uk.gov.justice.digital.hmpps.integrations.delius.person.ProbationCaseRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.CaseOffence
import uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity.MainOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance.PersonalCircumstanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.personalcircumstance.entity.PersonalCircumstanceType
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate

@Service
class CaseService(
    private val probationCaseRepository: ProbationCaseRepository,
    private val registrationRepository: RegistrationRepository,
    private val offenceRepository: MainOffenceRepository,
    private val personalCircumstanceRepository: PersonalCircumstanceRepository,
    private val disposalRepository: DisposalRepository,
) {
    fun getCaseSummaries(ids: List<String>): CaseSummaries =
        CaseSummaries(probationCaseRepository.findByCrnInOrNomsIdIn(ids).map { it.summary() })

    fun getCaseDetail(id: String): CaseDetail {
        val person = probationCaseRepository.findByCrnOrNomsId(id)
            ?: throw NotFoundException("ProbationCase", "CRN or NOMIS id", id)
        val registrations = registrationRepository.findByPersonId(person.id)
        val offences = offenceRepository.findOffencesFor(person.id)
        val circumstances = personalCircumstanceRepository.findByPersonId(person.id)
        val sentences = disposalRepository.findSentences(person.id).map { disposal ->
            Sentence(
                typeDescription = disposal.type.description,
                startDate = disposal.startDate,
                endDate = disposal.expectedEndDate(),
                eventNumber = disposal.event.number
            )
        }
        return person.summary().withDetail(offences, registrations, circumstances.map { it.type.code }, sentences)
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
    circumstances: List<String>,
    sentences: List<Sentence>
): CaseDetail {
    val regMap = registrations.groupBy { it.type.code == RegisterType.Code.MAPPA.value }
    return CaseDetail(
        this,
        offences.map { it.asOffence() },
        regMap.flags(),
        regMap.mappa(),
        circumstances.isCareLeaver(),
        circumstances.isVeteran(),
        sentences
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

fun CaseOffence.asOffence() =
    Offence(
        id = if (main) "M$id" else "A$id",
        code,
        description,
        mainCategoryDescription,
        subCategoryDescription,
        date,
        main,
        eventId,
        eventNumber
    )

fun Registration.asRegistration() = uk.gov.justice.digital.hmpps.model.Registration(
    code = type.code,
    description = type.description,
    startDate = date,
    riskNotes = formatNote(notes, truncateNote = false)
)

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


val NOTE_HEADER_REGEX = Regex(
    "^Comment added by (.+?) on (\\d{2}/\\d{2}/\\d{4}) at \\d{2}:\\d{2}${System.lineSeparator()}"
)

fun formatNote(notes: String?, truncateNote: Boolean): List<NoteDetail> {
    val splitParam = "---------------------------------------------------------" + System.lineSeparator()

    return buildList {
        notes
            ?.takeIf { it.isNotBlank() }
            ?.split(splitParam)
            ?.asReversed()
            ?.forEachIndexed { index, note ->

                val match = NOTE_HEADER_REGEX.find(note)
                val header = match?.value

                val commentText = (header?.let { note.removePrefix(it) } ?: note).trimEnd()
                if (commentText.isBlank() || commentText == "null") return@forEachIndexed

                val userCreatedBy = match?.groupValues?.getOrNull(1)
                val dateCreatedBy = match?.groupValues?.getOrNull(2)
                    ?.let { LocalDate.parse(it, DeliusDateFormatter) }

                val finalText = if (truncateNote) commentText.take(1500) else commentText
                val truncatedFlag = if (truncateNote) commentText.length > 1500 else null

                add(
                    NoteDetail(
                        id = index,
                        createdBy = userCreatedBy,
                        createdByDate = dateCreatedBy,
                        note = finalText,
                        hasNoteBeenTruncated = truncatedFlag
                    )
                )
            }
    }
}
