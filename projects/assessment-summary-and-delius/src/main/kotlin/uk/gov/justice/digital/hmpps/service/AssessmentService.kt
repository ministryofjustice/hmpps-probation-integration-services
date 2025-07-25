package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
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
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.Dataset.Code.OASYS_ASSESSMENT_STATUS
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary
import uk.gov.justice.digital.hmpps.integrations.oasys.Objective
import java.time.LocalDate

@Service
class AssessmentService(
    private val courtRepository: CourtRepository,
    private val offenceRepository: OffenceRepository,
    private val oasysAssessmentRepository: OasysAssessmentRepository,
    private val eventRepository: EventRepository,
    private val contactService: ContactService,
    private val referenceDataRepository: ReferenceDataRepository,
) {
    fun recordAssessment(person: Person, summary: AssessmentSummary): Contact {
        val previousAssessment = oasysAssessmentRepository.findByOasysId(summary.assessmentPk.toString())

        val eventNumber = summary.furtherInformation.cmsEventNumber?.toString()
            ?: eventRepository.findActiveCustodialEvents(person.id).singleOrNull()
            ?: throw IgnorableMessageException("No single active custodial event")
        val event = eventRepository.getByNumber(person.id, eventNumber)
        checkNotNull(person.manager) { "Community Manager Not Found" }
        val contactDetail = summary.contactDetail()
        val contact = previousAssessment?.contact
            ?.takeIf { it.type.code == contactDetail.typeCode.value }
            ?.updateWithDetail(contactDetail)
            ?: contactService.createContact(contactDetail, person, event, previousAssessment?.contact)

        previousAssessment?.also(oasysAssessmentRepository::delete)
        oasysAssessmentRepository.save(summary.oasysAssessment(person, event, contact))
        return contact
    }

    fun AssessmentSummary.oasysAssessment(person: Person, event: Event, contact: Contact): OasysAssessment {
        val assessment = OasysAssessment(
            oasysId = assessmentPk.toString(),
            date = dateCompleted ?: initiationDate,
            person = person,
            eventNumber = event.number,
            contact = contact,
            court = furtherInformation.courtCode
                ?.let { courtRepository.getByCode(it) },
            offence = offences
                ?.firstOrNull { it.offenceCode != null && it.offenceSubcode != null }
                ?.let { offenceRepository.findByCode(it.offenceCode + it.offenceSubcode) },
            totalScore = furtherInformation.totWeightedScore,
            description = furtherInformation.pOAssessmentDesc,
            assessedBy = furtherInformation.assessorName?.let { if (it.length > 35) initialiseName(it) else it },
            riskFlags = riskFlags.joinToString(","),
            concernFlags = concernFlags.joinToString(","),
            dateCreated = initiationDate,
            dateReceived = LocalDate.now(),
            initialSentencePlanDate = initialSpDate,
            sentencePlanReviewDate = reviewSpDate,
            reviewTerminated = furtherInformation.reviewTerm?.equals("Y", true),
            reviewNumber = reviewNum,
            layerType = assessmentType,
            ogrsScore1 = furtherInformation.ogrs1Year,
            ogrsScore2 = furtherInformation.ogrs2Year,
            ogpScore1 = ogpOvp.ogp1Year,
            ogpScore2 = ogpOvp.ogp2Year,
            ovpScore1 = ogpOvp.ovp1Year,
            ovpScore2 = ogpOvp.ovp2Year,
            status = assessmentStatus.asOasysStatus()
        ).withSectionScores(weightedScores)
        sentencePlan?.objectives?.map { it.plan(person, assessment) }
            ?.forEach { assessment.withSentencePlan(it) }
        return assessment
    }

    private fun initialiseName(name: String): String {
        val names = name.split(Regex("\\s+")).toMutableList()
        for (i in 0 until names.size - 1) names[i] = "${names[i].first()}."
        return names.joinToString(" ")
    }

    private fun String.asOasysStatus(): ReferenceData? = when (this) {
        "COMPLETE" -> "C"
        "LOCKED_INCOMPLETE" -> "LI"
        else -> null
    }?.let {
        referenceDataRepository.findByCode(it, OASYS_ASSESSMENT_STATUS.value)
    }
}

private fun AssessmentSummary.contactDetail() =
    ContactDetail(
        when (assessmentStatus) {
            "COMPLETE" -> ContactType.Code.OASYS_ASSESSMENT_COMPLETE
            "LOCKED_INCOMPLETE" -> ContactType.Code.OASYS_ASSESSMENT_LOCKED_INCOMPLETE
            else -> throw IllegalArgumentException("Unexpected assessment status: $assessmentStatus")
        },
        dateCompleted ?: initiationDate,
        "Reason for Assessment: ${furtherInformation.pOAssessmentDesc}",
        "urn:uk:gov:hmpps:oasys:assessment:${assessmentPk}"
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
    criminogenicNeeds?.forEachIndexed { index, need -> sp.withNeed(index.deliusIndex(), need.criminogenicNeedDesc) }
    actions?.forEachIndexed { index, action ->
        if (action.actionDesc != null) {
            sp.withWorkSummary(index.deliusIndex(), action.actionDesc)
            action.actionComment?.also {
                sp.withText(index.deliusIndex(), it)
            }
        }
    }
    return sp
}
