package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AdditionalSentence

object AdditionalSentenceGenerator {

    val REF_DISQ = generateReferenceData("DISQ", "Disqualified from Driving")
    val REF_FINE = generateReferenceData("FINE", "Fine")

    val SENTENCE_DISQ = generateSentence(length = 3, referenceData = REF_DISQ, event = PersonGenerator.EVENT_1)
    val SENTENCE_FINE =
        generateSentence(amount = 500, referenceData = REF_FINE, notes = "fine notes", event = PersonGenerator.EVENT_2)

    fun generateSentence(
        length: Long? = null,
        amount: Long? = null,
        notes: String? = null,
        event: Event,
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

    private fun generateReferenceData(code: String, description: String) =
        ReferenceData(IdGenerator.getAndIncrement(), code, description)
}

