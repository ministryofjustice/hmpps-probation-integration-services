package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessment
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.SentencePlan
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.CourtRepository
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.OffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.court.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByNumber
import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary
import uk.gov.justice.digital.hmpps.integrations.oasys.Objective
import uk.gov.justice.digital.hmpps.integrations.oasys.PurposeOfAssessmentMapping
import java.time.LocalDate

@Service
class AssessmentService(
    private val courtRepository: CourtRepository,
    private val offenceRepository: OffenceRepository,
    private val oasysAssessmentRepository: OasysAssessmentRepository,
    private val eventRepository: EventRepository,
    private val contactService: ContactService
) {
    fun recordAssessment(person: Person, summary: AssessmentSummary) {
        val previousAssessment = oasysAssessmentRepository.findByOasysId(summary.assessmentPk.toString())

        val eventNumber = summary.furtherInformation.cmsEventNumber?.toString()
            ?: throw IllegalArgumentException("No Event Number provided")
        val event = eventRepository.getByNumber(person.id, eventNumber)
        val manager = checkNotNull(person.manager) { "Community Manager Not Found" }
        val contact = previousAssessment?.contact?.withDateTeamAndStaff(
            LocalDate.now(),
            manager.teamId,
            manager.staffId
        ) ?: contactService.createContact(
            summary.contactDetail(),
            person,
            event
        )

        previousAssessment?.also(oasysAssessmentRepository::delete)
        oasysAssessmentRepository.save(summary.oasysAssessment(person, event, contact))
    }

    fun AssessmentSummary.oasysAssessment(person: Person, event: Event, contact: Contact): OasysAssessment {
        val assessment = OasysAssessment(
            assessmentPk.toString(),
            dateCompleted,
            person,
            event.number,
            contact,
            furtherInformation.courtCode?.let { courtRepository.getByCode(it) },
            offences.firstOrNull { it.offenceCode != null && it.offenceSubcode != null }
                ?.let { offenceRepository.getByCode(it.offenceCode + it.offenceSubcode) },
            furtherInformation.totWeightedScore,
            furtherInformation.pOAssessment?.let {
                PurposeOfAssessmentMapping[it] ?: throw IllegalArgumentException("Unexpected 'pOAssessment' code '$it'")
            },
            furtherInformation.assessorName,
            riskFlags.joinToString(","),
            concernFlags.joinToString(","),
            initiationDate,
            LocalDate.now(),
            initialSpDate,
            reviewSpDate,
            furtherInformation.reviewTerm?.equals("Y", true),
            reviewNum,
            assessmentType,
            furtherInformation.ogrs1Year,
            furtherInformation.ogrs2Year,
            ogpOvp.ogp1Year,
            ogpOvp.ogp2Year,
            ogpOvp.ovp1Year,
            ogpOvp.ovp2Year,
        ).withSectionScores(weightedScores)
        sentencePlan?.objectives?.map { it.plan(person, assessment) }
            ?.forEach { assessment.withSentencePlan(it) }
        return assessment
    }
}

private fun AssessmentSummary.contactDetail() =
    ContactDetail(
        ContactType.Code.OASYS_ASSESSMENT,
        LocalDate.now(),
        "Reason for Assessment: ${furtherInformation.pOAssessmentDesc}"
    )

fun Objective.plan(
    person: Person,
    assessment: OasysAssessment
): SentencePlan {
    fun Int.deliusIndex() = this + 1L
    val sp = SentencePlan(
        person,
        assessment,
        objectiveSequence,
        objectiveCodeDesc
    )
    criminogenicNeeds.forEachIndexed { index, need -> sp.withNeed(index.deliusIndex(), need.criminogenicNeedDesc) }
    actions.forEachIndexed { index, action ->
        sp.withWorkSummary(index.deliusIndex(), action.actionDesc)
        action.actionComment?.also {
            sp.withText(index.deliusIndex(), it)
        }
    }
    return sp
}
