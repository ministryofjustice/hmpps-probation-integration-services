package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
import java.time.LocalDate

object SentenceGenerator {
    val DEFAULT_DISPOSAL_TYPE = generateDisposalType("DFS","Default Sentence Type", "NP", 0)
    val EVENT_WITH_NSI = generateEvent(PersonGenerator.DEFAULT.id)
    val SENTENCE_WITH_NSI = generateSentence(EVENT_WITH_NSI)
    val EVENT_WITHOUT_NSI = generateEvent(PersonGenerator.SENTENCED_WITHOUT_NSI.id)
    val SENTENCE_WITHOUT_NSI = generateSentence(EVENT_WITHOUT_NSI)

    val MAIN_CAT_F = RequirementMainCategory("F", IdGenerator.getAndIncrement())

    val OFFENCE = generateOffence()
    val FULL_DETAIL_EVENT = generateEvent(CaseDetailsGenerator.FULL_PERSON.id)
    val FULL_DETAIL_MAIN_OFFENCE = generateMainOffence(FULL_DETAIL_EVENT, OFFENCE)
    val FULL_DETAIL_SENTENCE = generateSentence(FULL_DETAIL_EVENT)

    fun generateEvent(
        personId: Long,
        convictionDate: LocalDate = LocalDate.now().minusDays(14),
        ftcCount: Long = 0,
        inBreach: Boolean = false,
        breachEnd: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(personId, convictionDate, null, ftcCount, inBreach, breachEnd, null, active, softDeleted, id)

    fun generateDisposalType(
        code: String,
        description: String,
        sentenceType: String? = null,
        ftcLimit: Long? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = DisposalType(code, description, sentenceType, ftcLimit, id)

    fun generateSentence(
        event: Event,
        date: LocalDate = LocalDate.now().minusDays(14),
        type: DisposalType = DEFAULT_DISPOSAL_TYPE,
        enteredEndDate: LocalDate? = null,
        notionalEndDate: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, date, type, enteredEndDate, notionalEndDate, active, softDeleted, id)

    fun generateMainOffence(
        event: Event,
        offence: Offence,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = MainOffence(event, offence, softDeleted, id)

    fun generateOffence(
        mainCategory: String = "Main Category",
        subCategory: String = "Sub Category",
        id: Long = IdGenerator.getAndIncrement()
    ) = Offence(mainCategory, subCategory, id)

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory = MAIN_CAT_F,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(disposal.event.personId, mainCategory, disposal.id, active, softDeleted, id)
}
