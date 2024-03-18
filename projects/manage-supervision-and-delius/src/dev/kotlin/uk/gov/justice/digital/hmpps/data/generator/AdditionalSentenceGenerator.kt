package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentence

object AdditionalSentenceGenerator {

    val DISQ = generateReferenceDate("DISQ", "Disqualified from Driving")
    val FINE = generateReferenceDate("FINE", "Fine")

    val SENTENCE_DISQ = generateSentence(length = 3, referenceData = DISQ)
    val SENTENCE_FINE = generateSentence(amount = 500, referenceData = FINE)
    fun generateSentence(
        length: Long? = null,
        amount: Long? = null,
        notes: String? = null,
        event: Event? = null,
        referenceData: ReferenceData
    ) = AdditionalSentence(
        IdGenerator.getAndIncrement(),
        length,
        amount,
        notes,
        false,
        event,
        referenceData
        )

    fun generateReferenceDate(code: String, description: String) =
        ReferenceData(IdGenerator.getAndIncrement(), code, description)
}

