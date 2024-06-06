package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.REF_DISQ
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENTLY_MANAGED
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.AdditionalSentence
import java.math.BigDecimal

object AdditionalSentenceGenerator {

    val SENTENCE_DISQ =
        generateSentence(amount = BigDecimal(7), length = 3, referenceData = REF_DISQ, event = CURRENTLY_MANAGED)

    fun generateSentence(
        length: Long? = null,
        amount: BigDecimal? = null,
        notes: String? = null,
        event: Event,
        referenceData: ReferenceData
    ) = AdditionalSentence(
        IdGenerator.getAndIncrement(),
        event,
        referenceData,
        amount,
        length,
        notes,
        false,
    )
}