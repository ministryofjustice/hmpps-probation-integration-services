package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentence

object AdditionalSentenceGenerator {

    val REF_DISQ = generateReferenceDate("DISQ", "Disqualified from Driving")
    val REF_FINE = generateReferenceDate("FINE", "Fine")

    val SENTENCE_DISQ = generateSentence(length = 3, referenceData = REF_DISQ)
    val SENTENCE_FINE = generateSentence(amount = 500, referenceData = REF_FINE, notes = "fine notes")
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

