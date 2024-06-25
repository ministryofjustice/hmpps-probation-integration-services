package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_SENTENCE
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Requirement

object RequirementsGenerator {

    val REQ1 = Requirement(
        IdGenerator.getAndIncrement(),
        CURRENT_SENTENCE.id,
        "notes"
    )
}