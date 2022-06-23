package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff

object StaffGenerator {
    val DEFAULT = Staff(
        IdGenerator.getAndIncrement(),
        "Bob",
        "Smith",
        "${ProbationAreaGenerator.DEFAULT.code}A999",
        ProbationAreaGenerator.DEFAULT.id,
        lastModifiedUserId = UserGenerator.APPLICATION_USER.id,
        createdByUserId = UserGenerator.APPLICATION_USER.id,
    )
}
