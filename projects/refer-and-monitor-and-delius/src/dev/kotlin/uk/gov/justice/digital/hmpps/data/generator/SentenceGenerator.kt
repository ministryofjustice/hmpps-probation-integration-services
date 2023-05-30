package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory
import java.time.LocalDate

object SentenceGenerator {
    val DEFAULT_DISPOSAL_TYPE = generateDisposalType("NP", 0)
    val EVENT_WITH_NSI = generateEvent(PersonGenerator.DEFAULT.id)
    val SENTENCE_WITH_NSI = generateSentence(EVENT_WITH_NSI, type = DEFAULT_DISPOSAL_TYPE)
    val EVENT_WITHOUT_NSI = generateEvent(PersonGenerator.SENTENCED_WITHOUT_NSI.id)
    val SENTENCE_WITHOUT_NSI = generateSentence(EVENT_WITHOUT_NSI, type = DEFAULT_DISPOSAL_TYPE)

    val MAIN_CAT_F = RequirementMainCategory("F", IdGenerator.getAndIncrement())

    fun generateEvent(
        personId: Long,
        ftcCount: Long = 0,
        inBreach: Boolean = false,
        breachEnd: LocalDate? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(personId, null, ftcCount, inBreach, breachEnd, active, softDeleted, id)

    fun generateDisposalType(
        sentenceType: String? = null,
        ftcLimit: Long? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = DisposalType(sentenceType, ftcLimit, id)

    fun generateSentence(
        event: Event,
        date: LocalDate = LocalDate.now().minusDays(14),
        type: DisposalType,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, date, type, softDeleted, id)

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory = MAIN_CAT_F,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(disposal.event.personId, mainCategory, disposal.id, active, softDeleted, id)
}
