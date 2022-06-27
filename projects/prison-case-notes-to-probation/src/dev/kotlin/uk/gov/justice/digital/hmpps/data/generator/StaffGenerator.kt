package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff

object StaffGenerator {
    var DEFAULT = generate(
        "${ProbationAreaGenerator.DEFAULT.code}A999",
        "Bob",
        "Smith",
    )

    fun generate(
        code: String,
        forename: String,
        surname: String,
        id: Long = IdGenerator.getAndIncrement(),
        probationAreaId: Long = ProbationAreaGenerator.DEFAULT.id,
    ): Staff {
        return Staff(
            id,
            forename,
            surname,
            code,
            probationAreaId,
        )
    }
}
