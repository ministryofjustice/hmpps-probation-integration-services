package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Disability
import java.time.ZonedDateTime

object DisabilityGenerator {
    val DISABILITY = Disability(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.PERSON1.id,
        type = ReferenceDataGenerator.DISABILITY_TYPE_VISUAL,
        lastUpdated = ZonedDateTime.parse("2024-06-15T10:30:00+01:00"),
        softDeleted = false,
    )
}

