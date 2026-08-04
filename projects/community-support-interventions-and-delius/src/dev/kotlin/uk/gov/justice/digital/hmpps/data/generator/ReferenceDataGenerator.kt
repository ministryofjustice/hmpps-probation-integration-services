package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val LANGUAGE_ENGLISH = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "ENG",
        description = "English",
    )

    val DISABILITY_TYPE_VISUAL = ReferenceData(
        id = IdGenerator.getAndIncrement(),
        code = "VI",
        description = "Visual Impairment",
    )
}

