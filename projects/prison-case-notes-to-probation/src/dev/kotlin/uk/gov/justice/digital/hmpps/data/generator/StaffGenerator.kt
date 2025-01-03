package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff

object StaffGenerator {
    var DEFAULT = generate(
        "${ProbationAreaGenerator.DEFAULT.code}A999",
        "Bob",
        "Smith"
    )

    fun generate(
        code: String,
        forename: String,
        surname: String,
        probationAreaId: Long = ProbationAreaGenerator.DEFAULT.id
    ): Staff {
        return Staff(
            forename,
            surname,
            code,
            probationAreaId
        )
    }
}
