package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.documents.entity.Person

object PersonGenerator {
    val DEFAULT = Person(
        id = IdGenerator.getAndIncrement(),
        forename = "First",
        secondName = "Middle",
        thirdName = null,
        surname = "Last",
        crn = "A000001",
        nomisId = "A0001AA",
        events = listOf(),
        softDeleted = false
    )
}
