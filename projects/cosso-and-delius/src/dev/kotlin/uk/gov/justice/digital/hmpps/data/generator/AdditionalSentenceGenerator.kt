package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.DEFAULT_EVENT
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_ADDITIONAL_SENTENCE_TYPE
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.DEFAULT_UNITS
import uk.gov.justice.digital.hmpps.entity.AdditionalSentence
import java.math.BigDecimal

object AdditionalSentenceGenerator {

    val DEFAULT_ADDITIONAL_SENTENCE = AdditionalSentence(
        id = IdGenerator.getAndIncrement(),
        length = 2L,
        amount = BigDecimal.valueOf(100),
        notes = "Additional sentence notes",
        eventId = DEFAULT_EVENT.eventId,
        type = DEFAULT_ADDITIONAL_SENTENCE_TYPE ,
        units = DEFAULT_UNITS
    )

}
