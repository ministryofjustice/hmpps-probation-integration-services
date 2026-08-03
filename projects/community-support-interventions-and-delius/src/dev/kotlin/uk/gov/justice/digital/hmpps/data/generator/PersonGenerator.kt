package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person

object PersonGenerator {
    val PERSON1 = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "X123456",
        language = ReferenceDataGenerator.LANGUAGE_ENGLISH,
        softDeleted = false,
    )
    val PERSON2 = Person(
        id = IdGenerator.getAndIncrement(),
        crn = "X123457",
        softDeleted = false,
    )
}