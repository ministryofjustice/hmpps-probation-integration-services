package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_SENTENCE
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Requirement
import java.time.LocalDate

object RequirementsGenerator {

    val REQ1 = Requirement(
        IdGenerator.getAndIncrement(),
        CURRENT_SENTENCE.id,
        "notes",
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 1, 2),
        LocalDate.of(2024, 1, 3),
        LocalDate.of(2024, 1, 4),
        LocalDate.of(2024, 1, 5),
        ReferenceDataGenerator.REQUIREMENT_SUB_CAT,
        ReferenceDataGenerator.REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_SUB_CAT,
        ReferenceDataGenerator.TERMINATION_REASON,
        12,
        true
    )
}