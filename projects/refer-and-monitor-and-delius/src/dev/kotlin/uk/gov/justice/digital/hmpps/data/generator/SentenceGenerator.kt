package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.RequirementMainCategory

object SentenceGenerator {
    val SENTENCE_WITHOUT_NSI = generateSentence(PersonGenerator.SENTENCED_WITHOUT_NSI.id)

    val MAIN_CAT_F = RequirementMainCategory("F", IdGenerator.getAndIncrement())

    fun generateSentence(
        personId: Long,
        eventId: Long = IdGenerator.getAndIncrement(),
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(personId, eventId, id)

    fun generateRequirement(
        disposal: Disposal,
        mainCategory: RequirementMainCategory = MAIN_CAT_F,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Requirement(disposal.personId, mainCategory, disposal.id, active, softDeleted, id)
}
